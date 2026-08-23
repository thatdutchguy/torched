package io.github.thatdutchguy.torched

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

private const val CAPACITY = 2.0
private const val REFILL_RATE = 1.0

class ThrowRateLimiterTest {
    @Test
    fun `enforces limits per id`() {
        val limiter = ThrowRateLimiter(CAPACITY, REFILL_RATE)
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val capacity = CAPACITY.toInt()
        repeat(capacity) { assertTrue(limiter.tryAcquire(id1, 0)) }
        repeat(capacity) { assertTrue(limiter.tryAcquire(id2, 0)) }
        assertFalse(limiter.tryAcquire(id1, 0))
        assertFalse(limiter.tryAcquire(id2, 0))
    }

    @Test
    fun `clears limits per uuid`() {
        val limiter = ThrowRateLimiter(CAPACITY, REFILL_RATE)
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val capacity = CAPACITY.toInt()
        repeat(capacity) { assertTrue(limiter.tryAcquire(id1, 0)) }
        repeat(capacity) { assertTrue(limiter.tryAcquire(id2, 0)) }
        assertFalse(limiter.tryAcquire(id1, 0))
        assertFalse(limiter.tryAcquire(id2, 0))

        limiter.clear(id1)
        assertFalse(limiter.tryAcquire(id2, 0))
        repeat(capacity) { assertTrue(limiter.tryAcquire(id1, 0)) }
        assertFalse(limiter.tryAcquire(id1, 0))

        limiter.clear(id2)
        assertFalse(limiter.tryAcquire(id1, 0))
        repeat(capacity) { assertTrue(limiter.tryAcquire(id2, 0)) }
        assertFalse(limiter.tryAcquire(id2, 0))
    }

    @Test
    fun `clears all limits`() {
        val limiter = ThrowRateLimiter(CAPACITY, REFILL_RATE)
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val capacity = CAPACITY.toInt()
        repeat(capacity) { assertTrue(limiter.tryAcquire(id1, 0)) }
        repeat(capacity) { assertTrue(limiter.tryAcquire(id2, 0)) }
        assertFalse(limiter.tryAcquire(id1, 0))
        assertFalse(limiter.tryAcquire(id2, 0))

        limiter.clearAll()
        repeat(capacity) { assertTrue(limiter.tryAcquire(id1, 0)) }
        repeat(capacity) { assertTrue(limiter.tryAcquire(id2, 0)) }
        assertFalse(limiter.tryAcquire(id1, 0))
        assertFalse(limiter.tryAcquire(id2, 0))
    }
}
