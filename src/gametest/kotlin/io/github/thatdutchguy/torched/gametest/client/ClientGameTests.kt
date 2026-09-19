@file:Suppress("UnstableApiUsage")

package io.github.thatdutchguy.torched.gametest.client

import com.mojang.blaze3d.platform.InputConstants
import io.github.thatdutchguy.torched.ModEntityTypes
import io.github.thatdutchguy.torched.ThrowRateLimit
import io.github.thatdutchguy.torched.ThrowTorchPayload
import io.github.thatdutchguy.torched.TorchThrowing
import io.github.thatdutchguy.torched.TorchedConfig
import io.github.thatdutchguy.torched.VanillaTorchThrowingPolicy
import io.github.thatdutchguy.torched.client.ModKeyBindings
import io.github.thatdutchguy.torched.client.VanillaTorchThrowingPolicyClient
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.block.Blocks

class ThrowByKeyIsAccepted : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) = context.withTorches(4) { singleplayer ->
        val throwKeyBinding = ModKeyBindings.THROW_TORCH
        context.onClient { check(!throwKeyBinding.isUnbound) { "setup: key binding is unbound" } }
        context.input.pressKey(throwKeyBinding)

        singleplayer.connection.waitForServerboundPackets()
        singleplayer.connection.waitForClientboundPackets()
        context.assertTorchCounts(singleplayer, clientCount = 3, serverCount = 3)
    }
}

class ThrowOffHandByKeyIsAccepted : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) =
        context.withTorches(4, InteractionHand.OFF_HAND) { singleplayer ->
            val throwKeyBinding = ModKeyBindings.THROW_TORCH
            context.onClient { check(!throwKeyBinding.isUnbound) { "setup: key binding is unbound" } }
            context.input.pressKey(throwKeyBinding)

            singleplayer.connection.waitForServerboundPackets()
            singleplayer.connection.waitForClientboundPackets()
            context.assertTorchCounts(singleplayer, hand = InteractionHand.OFF_HAND, clientCount = 3, serverCount = 3)
        }
}

class ThrowByMouseIsAccepted : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) = context.withTorches(4) { singleplayer ->
        context.onClient { check(TorchedConfig.data.throwOnUse) { "setup: throwOnUse is disabled" } }
        singleplayer.lookUp()
        context.pressRightMouseButton()

        singleplayer.connection.waitForServerboundPackets()
        singleplayer.connection.waitForClientboundPackets()
        context.assertTorchCounts(singleplayer, clientCount = 3, serverCount = 3)
    }
}

class ThrowIsAppliedLocally : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) = context.withTorches(4) { singleplayer ->
        context.onClient { TorchThrowing.applyThrowLocally(it.player!!, InteractionHand.MAIN_HAND) }
        context.assertTorchCounts(singleplayer, clientCount = 3, serverCount = 4)
    }
}

class UseHookDisabledPlacesInstead : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) = context.withTorches(4) { singleplayer ->
        val originalConfigData = TorchedConfig.data
        try {
            context.onClient { TorchedConfig.update { it.copy(throwOnUse = false) } }
            singleplayer.lookDown()
            context.pressRightMouseButton()

            singleplayer.connection.waitForServerboundPackets()
            singleplayer.connection.waitForClientboundPackets()
            context.assertTorchCounts(singleplayer, clientCount = 3, serverCount = 3)

            singleplayer.server.onServer { server ->
                val pos = server.playerList.players.first().blockPosition()
                val state = server.overworld().getBlockState(pos)
                check(state.`is`(Blocks.TORCH)) { "expected a placed torch at $pos, got ${state.block}" }
            }
        } finally {
            context.onClient { TorchedConfig.update { originalConfigData } }
        }
    }
}

class ThrowKeyRespectsBinding : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) = context.withTorches(4) { singleplayer ->
        val throwKeyBinding = ModKeyBindings.THROW_TORCH
        val keyCode = throwKeyBinding.defaultKey.value
        context.onClient {
            check(throwKeyBinding.isDefault) { "key binding not set to default" }
        }

        try {
            context.input.pressKey(keyCode)
            singleplayer.connection.waitForServerboundPackets()
            singleplayer.connection.waitForClientboundPackets()
            context.assertTorchCounts(singleplayer, clientCount = 3, serverCount = 3)

            context.onClient {
                check(throwKeyBinding.isDefault) { "key binding not set to default" }
                throwKeyBinding.setKey(InputConstants.UNKNOWN)
                KeyMapping.resetMapping()
            }

            context.input.pressKey(keyCode)
            singleplayer.connection.waitForServerboundPackets()
            singleplayer.connection.waitForClientboundPackets()
            context.assertTorchCounts(singleplayer, clientCount = 3, serverCount = 3)

        } finally {
            context.onClient {
                throwKeyBinding.setKey(throwKeyBinding.defaultKey)
                KeyMapping.resetMapping()
            }
        }
    }
}

// NOTE: This test exercises the internal code paths, rather than using a separate server to test a genuine disagreement
class RefusedThrowIsResynced : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) = context.withTorches(4) { singleplayer ->
        context.onClient { client ->
            val player = client.player!!
            // Optimistic shrink, as the real client does
            TorchThrowing.applyThrowLocally(player, InteractionHand.MAIN_HAND)
            check(player.mainHandItem.count == 3) { "#applyThrowLocally did not shrink the stack" }

            // Ask the server to throw from the empty offhand, which it will refuse
            ClientPlayNetworking.send(ThrowTorchPayload(InteractionHand.OFF_HAND))
        }

        singleplayer.connection.waitForServerboundPackets()
        singleplayer.connection.waitForClientboundPackets()

        context.assertTorchCounts(singleplayer, clientCount = 4, serverCount = 4)
    }
}

class ServerRateLimitIsEnforced : FabricClientGameTest {
    private val rateLimitCap = ThrowRateLimit.BURST_CAPACITY.toInt()
    private val initialTorchCount = rateLimitCap + 4

    override fun runTest(context: ClientGameTestContext) = context.withTorches(initialTorchCount) { singleplayer ->
        singleplayer.lookUp()
        context.onClient {
            // Bypass ClientThrowRateLimiting by sending payload directly
            repeat(rateLimitCap + 2) {
                ClientPlayNetworking.send(ThrowTorchPayload(InteractionHand.MAIN_HAND))
            }
        }

        singleplayer.connection.waitForServerboundPackets()
        singleplayer.connection.waitForClientboundPackets()

        val expectedTorchCount = initialTorchCount - rateLimitCap
        context.assertTorchCounts(singleplayer, clientCount = expectedTorchCount, serverCount = expectedTorchCount)
    }
}

class ServerRateLimitIsEnforcedInCreativeMode : FabricClientGameTest {
    private val rateLimitCap = ThrowRateLimit.BURST_CAPACITY.toInt()
    private val initialTorchCount = 32

    override fun runTest(context: ClientGameTestContext) = context.withTorches(initialTorchCount) { singleplayer ->
        singleplayer.server.runCommand("gamemode creative @p")
        singleplayer.connection.waitForClientboundPackets()

        singleplayer.lookUp()
        context.onClient { client ->
            // Bypass ClientThrowRateLimiting by sending payload directly
            repeat(rateLimitCap + 2) {
                ClientPlayNetworking.send(ThrowTorchPayload(InteractionHand.MAIN_HAND))
            }
        }

        singleplayer.connection.waitForServerboundPackets()
        singleplayer.server.onServer { server ->
            val player = server.playerList.players.single()
            val torches = player.level().getEntities(
                ModEntityTypes.THROWABLE_TORCH_ENTITY,
                player.boundingBox.inflate(32.0),
            ) { entity ->
                entity.isAlive
            }
            check(rateLimitCap == torches.size) { "expected ${rateLimitCap} torches, got ${torches.size}" }
        }

        context.assertTorchCounts(singleplayer, clientCount = initialTorchCount, serverCount = initialTorchCount)
    }
}

//region VanillaTorchThrowingPolicy

class VanillaTorchThrowingPolicyPublishes : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) = context.preservingConfig {
        TorchedConfig.update { it.copy(throwVanillaTorches = false) }

        // assert initially unset
        checkServerPolicy(context, null)

        context.worldBuilder().create().use { singleplayer ->
            singleplayer.server.onServer {
                VanillaTorchThrowingPolicy.publish(true)
            }
            singleplayer.connection.waitForClientboundPackets()
            checkServerPolicy(context, true)

            singleplayer.server.onServer {
                VanillaTorchThrowingPolicy.publish(false)
            }
            singleplayer.connection.waitForClientboundPackets()
            checkServerPolicy(context, false)
        }

        checkServerPolicy(context, null)
    }
}

class VanillaThrowRespectsServerPolicy : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) = context.preservingConfig {

        fun publishTorchThrowingPolicy(singleplayer: TestSingleplayerContext, allowed: Boolean) {
            singleplayer.server.onServer {
                VanillaTorchThrowingPolicy.publish(allowed)
            }
            singleplayer.connection.waitForClientboundPackets()
        }

        fun checkCounts(singleplayer: TestSingleplayerContext, clientCount: Int, serverCount: Int) {
            // NOTE: this doesn't necessarily test no throw was predicted by the client, because the
            // helper methods that press a mouse button or key also wait one tick, and a correction
            // could have been sent in that time.
            context.onClient { client ->
                val count = client.player!!.getItemInHand(InteractionHand.MAIN_HAND).count
                check(count == clientCount) { "client: expected $clientCount torches, got $count" }
            }
            singleplayer.connection.waitForServerboundPackets()
            singleplayer.connection.waitForClientboundPackets()
            context.assertTorchCounts(singleplayer, clientCount = clientCount, serverCount = serverCount)
        }

        fun mouseThrowThenCheck(singleplayer: TestSingleplayerContext, clientCount: Int, serverCount: Int) {
            context.pressRightMouseButton()
            checkCounts(singleplayer, clientCount, serverCount)
        }

        fun keyboardThrowThenCheck(singleplayer: TestSingleplayerContext, clientCount: Int, serverCount: Int) {
            val throwKeyBinding = ModKeyBindings.THROW_TORCH
            context.onClient { check(!throwKeyBinding.isUnbound) { "setup: key binding is unbound" } }
            context.input.pressKey(throwKeyBinding)
            checkCounts(singleplayer, clientCount, serverCount)
        }

        TorchedConfig.update { it.copy(throwVanillaTorches = true) }
        context.withTorches(4, torchType = "minecraft:torch") { singleplayer ->
            publishTorchThrowingPolicy(singleplayer, false)
            checkPolicies(context, serverAllowed = false, clientAllowed = true)
            mouseThrowThenCheck(singleplayer, clientCount = 4, serverCount = 4)
            keyboardThrowThenCheck(singleplayer, clientCount = 4, serverCount = 4)
        }

        TorchedConfig.update { it.copy(throwVanillaTorches = false) }
        context.withTorches(4, torchType = "minecraft:torch") { singleplayer ->
            publishTorchThrowingPolicy(singleplayer, true)
            checkPolicies(context, serverAllowed = true, clientAllowed = false)
            mouseThrowThenCheck(singleplayer, clientCount = 4, serverCount = 4)
            keyboardThrowThenCheck(singleplayer, clientCount = 4, serverCount = 4)
        }

        TorchedConfig.update { it.copy(throwVanillaTorches = false) }
        context.withTorches(4, torchType = "minecraft:torch") { singleplayer ->
            checkPolicies(context, serverAllowed = false, clientAllowed = false)
            mouseThrowThenCheck(singleplayer, clientCount = 4, serverCount = 4)
            keyboardThrowThenCheck(singleplayer, clientCount = 4, serverCount = 4)
        }

        TorchedConfig.update { it.copy(throwVanillaTorches = true) }
        context.withTorches(4, torchType = "minecraft:torch") { singleplayer ->
            checkPolicies(context, serverAllowed = true, clientAllowed = true)
            mouseThrowThenCheck(singleplayer, clientCount = 3, serverCount = 3)
            keyboardThrowThenCheck(singleplayer, clientCount = 2, serverCount = 2)
        }
    }
}

private fun checkServerPolicy(context: ClientGameTestContext, serverAllowed: Boolean?) {
    context.onClient {
        check(serverAllowed == VanillaTorchThrowingPolicyClient.permissions.serverAllowed) {
            "client: expected serverAllowed to be $serverAllowed"
        }
    }
}

private fun checkClientPolicy(context: ClientGameTestContext, clientAllowed: Boolean) {
    context.onClient {
        check(clientAllowed == VanillaTorchThrowingPolicyClient.permissions.clientAllowed) {
            "client: expected clientAllowed to be $clientAllowed"
        }
    }
}

private fun checkPolicies(context: ClientGameTestContext, serverAllowed: Boolean?, clientAllowed: Boolean) {
    checkServerPolicy(context, serverAllowed)
    checkClientPolicy(context, clientAllowed)
}

//endregion

//region Test Helpers

private fun ClientGameTestContext.preservingConfig(body: () -> Unit) {
    val originalConfigData = TorchedConfig.data
    try {
        body()
    } finally {
        onClient { TorchedConfig.update { originalConfigData } }
    }
}

private fun ClientGameTestContext.withTorches(
    count: Int,
    hand: InteractionHand = InteractionHand.MAIN_HAND,
    torchType: String = "torched:sticky_torch",
    body: (TestSingleplayerContext) -> Unit
) {
    val slot = when (hand) {
        InteractionHand.MAIN_HAND -> "weapon.mainhand"
        InteractionHand.OFF_HAND -> "weapon.offhand"
    }
    worldBuilder().create().use { singleplayer ->
        singleplayer.connection.waitForChunksRender()
        singleplayer.server.runCommand("item replace entity @p $slot with $torchType $count")
        singleplayer.connection.waitForClientboundPackets()

        onClient { client ->
            val actual = client.player!!.getItemInHand(hand).count
            check(actual == count) { "setup: expected $count torches, got $actual" }
        }

        body(singleplayer)
    }
}

private fun ClientGameTestContext.assertTorchCounts(
    singleplayer: TestSingleplayerContext,
    clientCount: Int,
    serverCount: Int,
    hand: InteractionHand = InteractionHand.MAIN_HAND
) {
    onClient { client ->
        val count = client.player!!.getItemInHand(hand).count
        check(count == clientCount) { "client: expected $clientCount torches, got $count" }
    }
    singleplayer.server.onServer { server ->
        val count = server.playerList.players.first().getItemInHand(hand).count
        check(count == serverCount) { "server: expected $serverCount torches, got $count" }
    }
}

private fun TestSingleplayerContext.lookUp() {
    server.runCommand("tp @p ~ ~ ~ ~ -90")
    connection.waitForClientboundPackets()
}

private fun TestSingleplayerContext.lookDown() {
    server.runCommand("tp @p ~ ~ ~ ~ 90")
    connection.waitForClientboundPackets()
}

private fun ClientGameTestContext.onClient(action: (Minecraft) -> Unit) = runOnClient<Throwable>(action)
private fun TestServerContext.onServer(action: (MinecraftServer) -> Unit) = runOnServer<Throwable>(action)
private fun ClientGameTestContext.pressRightMouseButton() = input.pressMouse(InputConstants.MOUSE_BUTTON_RIGHT)

//endregion