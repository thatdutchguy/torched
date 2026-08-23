package io.github.thatdutchguy.torched

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import java.util.UUID

object ServerThrowRateLimiter {
    private val limiter = ThrowRateLimiter()

    fun tryConsume(playerId: UUID, tick: Int): Boolean =
        limiter.tryAcquire(playerId, tick)

    fun initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            limiter.clear(handler.player.uuid)
        }
    }
}
