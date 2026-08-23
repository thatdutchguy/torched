package io.github.thatdutchguy.torched.client

import io.github.thatdutchguy.torched.ModEntityTypes
import io.github.thatdutchguy.torched.ThrowTorchPayload
import io.github.thatdutchguy.torched.TorchThrowing
import io.github.thatdutchguy.torched.TorchedConfig
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.player.ItemEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

object TorchedClient : ClientModInitializer {
    private var tickCount: Int = 0

    override fun onInitializeClient() {
        EntityRenderers.register(ModEntityTypes.THROWABLE_TORCH_ENTITY, ::ThrownItemRenderer)

        ClientThrowRateLimiter.initialize()
        ClientTickEvents.END_CLIENT_TICK.register { _ -> tickCount++ }

        ModKeyBindings.initialize()
        ClientTickEvents.END_CLIENT_TICK.register(::handleThrowKey)
        ItemEvents.USE.register(::handleUse)
    }

    private fun handleUse(level: Level, player: Player, hand: InteractionHand): InteractionResult? {
        if (!level.isClientSide) return null // "use" trigger is client-side only
        if (!TorchedConfig.data.throwOnUse) return null
        if (!TorchThrowing.canThrow(player.getItemInHand(hand))) return null

        return sendThrow(player, hand)
    }

    private fun handleThrowKey(client: Minecraft) {
        var pressed = false
        while (ModKeyBindings.THROW_TORCH.consumeClick()) {
            pressed = true
        }

        if (!pressed) return
        val player = client.player ?: return
        val hand = throwableHand(player) ?: return
        sendThrow(player, hand)
    }

    private fun sendThrow(player: Player, hand: InteractionHand): InteractionResult? {
        if (!ClientPlayNetworking.canSend(ThrowTorchPayload.TYPE)) return null
        if (!ClientThrowRateLimiter.tryConsume(player.uuid, tickCount)) {
            return InteractionResult.FAIL
        }

        ClientPlayNetworking.send(ThrowTorchPayload(hand))
        TorchThrowing.applyThrowLocally(player, hand)
        return InteractionResult.SUCCESS
    }

    private fun throwableHand(player: LocalPlayer): InteractionHand? = when {
        TorchThrowing.canThrow(player.mainHandItem) -> InteractionHand.MAIN_HAND
        TorchThrowing.canThrow(player.offhandItem) -> InteractionHand.OFF_HAND
        else -> null
    }
}
