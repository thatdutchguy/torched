package io.github.thatdutchguy.torched.client

import io.github.thatdutchguy.torched.ThrowRateLimiter
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import java.util.UUID

object ClientThrowRateLimiter {
    private val limiter = ThrowRateLimiter()

    fun tryConsume(playerId: UUID, tick: Int): Boolean =
        limiter.tryAcquire(playerId, tick)

    fun initialize() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> limiter.clearAll() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> limiter.clearAll() }
    }
}

