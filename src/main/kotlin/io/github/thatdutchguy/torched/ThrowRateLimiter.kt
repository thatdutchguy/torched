package io.github.thatdutchguy.torched

import java.util.UUID

object ThrowRateLimit {
    const val BURST_CAPACITY: Double = 2.0
    const val REFILL_TOKENS_PER_TICK: Double = 1.0 / 4.0
}

class ThrowRateLimiter(
    private val burstCapacity: Double = ThrowRateLimit.BURST_CAPACITY,
    private val refillTokensPerTick: Double = ThrowRateLimit.REFILL_TOKENS_PER_TICK,
) {
    private val buckets = mutableMapOf<UUID, TokenBucket>()

    fun tryAcquire(playerId: UUID, currentTick: Int) = bucketFor(playerId, currentTick).tryConsume(currentTick)

    fun clear(playerId: UUID) {
        buckets.remove(playerId)
    }

    fun clearAll() = buckets.clear()

    private fun bucketFor(playerId: UUID, currentTick: Int) =
        buckets.getOrPut(playerId) { createBucket(currentTick) }

    private fun createBucket(currentTick: Int) =
        TokenBucket(capacity = burstCapacity, refillTokensPerTick = refillTokensPerTick, currentTick)
}
