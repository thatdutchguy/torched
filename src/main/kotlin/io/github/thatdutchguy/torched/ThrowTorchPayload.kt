package io.github.thatdutchguy.torched

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.InteractionHand

data class ThrowTorchPayload(val hand: InteractionHand) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ThrowTorchPayload> =
            CustomPacketPayload.Type<ThrowTorchPayload>(Torched.id("throw_torch"))

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, ThrowTorchPayload> = CustomPacketPayload.codec(
            { payload: ThrowTorchPayload, buf: FriendlyByteBuf ->
                buf.writeBoolean(payload.hand == InteractionHand.OFF_HAND)
            },
            { buf: FriendlyByteBuf ->
                ThrowTorchPayload(
                    when {
                        buf.readBoolean() -> InteractionHand.OFF_HAND
                        else -> InteractionHand.MAIN_HAND
                    }
                )
            },
        )
    }
}
