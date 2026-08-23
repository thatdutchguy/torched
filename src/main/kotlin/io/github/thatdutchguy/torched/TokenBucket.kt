package io.github.thatdutchguy.torched

class TokenBucket(
    private val capacity: Double,
    private val refillTokensPerTick: Double,
    currentTick: Int,
) {
    private var tokens = capacity
    private var lastTick = currentTick

    fun tryConsume(currentTick: Int): Boolean {
        val elapsedTicks = currentTick - lastTick
        lastTick = currentTick

        val refilled = elapsedTicks * refillTokensPerTick
        tokens = minOf(capacity, tokens + refilled)

        if (tokens < 1.0) return false

        tokens -= 1.0
        return true
    }
}
