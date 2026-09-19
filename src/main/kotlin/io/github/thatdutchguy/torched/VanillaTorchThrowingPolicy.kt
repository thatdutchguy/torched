package io.github.thatdutchguy.torched

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

data class VanillaTorchThrowingPolicyPayload(val allowed: Boolean) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<VanillaTorchThrowingPolicyPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<VanillaTorchThrowingPolicyPayload> =
            CustomPacketPayload.Type<VanillaTorchThrowingPolicyPayload>(Torched.id("vanilla_torch_throw_policy"))

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, VanillaTorchThrowingPolicyPayload> =
            CustomPacketPayload.codec({ payload: VanillaTorchThrowingPolicyPayload, buf: FriendlyByteBuf ->
                buf.writeBoolean(payload.allowed)
            }, { buf: FriendlyByteBuf -> VanillaTorchThrowingPolicyPayload(buf.readBoolean()) })
    }
}

object VanillaTorchThrowingPolicy {
    private var serverInstance: MinecraftServer? = null

    fun initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register { serverInstance = it }
        ServerLifecycleEvents.SERVER_STOPPED.register { serverInstance = null }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            send(handler.player, TorchedConfig.data.throwVanillaTorches)
        }
    }

    fun publish(allowed: Boolean = TorchedConfig.data.throwVanillaTorches) {
        val server = serverInstance ?: return
        server.execute {
            for (player in PlayerLookup.all(server)) {
                send(player, allowed)
            }
        }
    }

    fun send(player: ServerPlayer, allowed: Boolean = TorchedConfig.data.throwVanillaTorches) {
        ServerPlayNetworking.send(player, VanillaTorchThrowingPolicyPayload(allowed))
    }
}
