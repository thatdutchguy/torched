package io.github.thatdutchguy.torched

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TokenBucketTest {
    @Test
    fun `consume up to initial capacity`() {
        val bucket = TokenBucket(capacity = 3.0, refillTokensPerTick = 1.00, currentTick = 0)
        repeat(3) { assertTrue(bucket.tryConsume(0)) }
        assertFalse(bucket.tryConsume(0))
    }

    @Test
    fun `refills tokens at configured rate`() {
        val bucket = TokenBucket(capacity = 3.0, refillTokensPerTick = 0.25, currentTick = 0)
        repeat(3) { assertTrue(bucket.tryConsume(0)) }
        assertFalse(bucket.tryConsume(0))
        assertFalse(bucket.tryConsume(1))
        assertFalse(bucket.tryConsume(2))
        assertFalse(bucket.tryConsume(3))
        assertTrue(bucket.tryConsume(4))
        assertFalse(bucket.tryConsume(4))
    }

    @Test
    fun `caps refill at capacity`() {
        val bucket = TokenBucket(capacity = 3.0, refillTokensPerTick = 0.25, currentTick = 0)
        repeat(3) { assertTrue(bucket.tryConsume(0)) }
        assertFalse(bucket.tryConsume(0))
        repeat(3) { assertTrue(bucket.tryConsume(1000)) }
        assertFalse(bucket.tryConsume(1000))
    }

    @Test
    fun `handles tick rollover`() {
        val refillRate = 0.25
        val startTick = Int.MAX_VALUE - 2
        val firstRefillTick = startTick + (1.0/refillRate).toInt()
        assertEquals(Int.MIN_VALUE + 1, firstRefillTick, "expected int to roll over")

        val bucket = TokenBucket(capacity = 3.0, refillRate, currentTick = startTick)
        repeat(3) { assertTrue(bucket.tryConsume(startTick)) }
        assertFalse(bucket.tryConsume(startTick))
        assertTrue(bucket.tryConsume(firstRefillTick))
        assertFalse(bucket.tryConsume(firstRefillTick))
    }
}
