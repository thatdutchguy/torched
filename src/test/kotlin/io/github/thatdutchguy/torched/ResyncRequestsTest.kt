package io.github.thatdutchguy.torched

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class ResyncRequestsTest {
    @Test
    fun `unsent ids are eligible`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val id3 = UUID.randomUUID()
        val id4 = UUID.randomUUID()
        val requests = ResyncRequests(cooldownTicks = 20)
        val handled = mutableSetOf<UUID>()

        requests.request(id1)
        requests.request(id2)
        requests.processEligible(1) { id -> handled.add(id) }
        assertEquals(setOf(id1, id2), handled)

        handled.clear()
        requests.request(id3)
        requests.request(id4)
        requests.processEligible(2) { id -> handled.add(id) }
        assertEquals(setOf(id3, id4), handled)
    }

    @Test
    fun `failed ids are eligible`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val id3 = UUID.randomUUID()
        val id4 = UUID.randomUUID()
        val requests = ResyncRequests(cooldownTicks = 20)
        val handled = mutableSetOf<UUID>()

        requests.request(id1)
        requests.request(id2)
        requests.processEligible(1) { false }
        requests.request(id3)
        requests.request(id4)
        requests.processEligible(2) { id -> handled.add(id) }
        assertEquals(setOf(id1, id2, id3, id4), handled)
    }

    @Test
    fun `sent ids honor cooldown`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val id3 = UUID.randomUUID()
        val id4 = UUID.randomUUID()
        val requests = ResyncRequests(cooldownTicks = 20)
        val handled = mutableSetOf<UUID>()

        requests.request(id1)
        requests.request(id2)
        requests.processEligible(1) { true }
        requests.request(id1)
        requests.request(id2)
        requests.request(id3)
        requests.request(id4)
        requests.processEligible(5) { id -> handled.add(id) }
        assertEquals(setOf(id3, id4), handled)

        handled.clear()
        requests.request(id3)
        requests.request(id4)
        requests.processEligible(20) { id -> handled.add(id) }
        assertTrue(handled.isEmpty())

        requests.processEligible(21) { id -> handled.add(id) }
        assertEquals(setOf(id1, id2), handled)

        handled.clear()
        requests.processEligible(24) { id -> handled.add(id) }
        assertTrue(handled.isEmpty())

        requests.processEligible(25) { id -> handled.add(id) }
        assertEquals(setOf(id3, id4), handled)
    }

    @Test
    fun `removed ids are not handled`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val id3 = UUID.randomUUID()
        val id4 = UUID.randomUUID()
        val requests = ResyncRequests(cooldownTicks = 20)
        val handled = mutableSetOf<UUID>()

        requests.request(id1)
        requests.request(id2)
        requests.request(id3)
        requests.request(id4)
        requests.clear(id2)
        requests.clear(id3)
        requests.processEligible(5) { id -> handled.add(id) }
        assertEquals(setOf(id1, id4), handled)
    }

    @Test
    fun `coalesces requests`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val requests = ResyncRequests(cooldownTicks = 20)
        val handled = mutableSetOf<UUID>()
        var callCount = 0

        requests.request(id1)
        requests.request(id1)
        requests.processEligible(0) {
            callCount++
            false
        }
        assertEquals(1, callCount)

        callCount = 0
        requests.request(id1)
        requests.request(id1)
        requests.processEligible(0) {
            callCount++
            false
        }
        assertEquals(1, callCount)

        callCount = 0
        requests.request(id2)
        requests.request(id2)
        requests.processEligible(0) { id ->
            callCount++
            handled.add(id)
        }
        assertEquals(2, callCount)
        assertEquals(setOf(id1, id2), handled)
    }

    @Test
    fun `state is reset after removal`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val requests = ResyncRequests(cooldownTicks = 20)
        val handled = mutableSetOf<UUID>()

        requests.request(id1)
        requests.request(id2)
        requests.processEligible(1) { id -> handled.add(id) }
        handled.clear()
        requests.request(id1)
        requests.request(id2)
        requests.processEligible(2) { id -> handled.add(id) }
        assertTrue(handled.isEmpty())

        requests.clear(id1)
        requests.request(id1)
        requests.processEligible(3) { id -> handled.add(id) }
        assertEquals(setOf(id1), handled)
    }

    @Test
    fun `handles tick rollover`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val cooldownTicks = 20
        val requests = ResyncRequests(cooldownTicks)
        val handled = mutableSetOf<UUID>()

        val startTick = Int.MAX_VALUE - 10
        val endCooldownTick = startTick + cooldownTicks
        assertEquals(Int.MIN_VALUE + 9, endCooldownTick, "expected int to roll over")

        requests.request(id1)
        requests.request(id2)
        requests.processEligible(startTick) { id -> handled.add(id) }
        assertEquals(setOf(id1, id2), handled)

        handled.clear()
        requests.request(id1)
        requests.request(id2)
        requests.processEligible(startTick) { id -> handled.add(id) }
        assertTrue(handled.isEmpty())

        requests.processEligible(Int.MIN_VALUE) { id -> handled.add(id) }
        assertTrue(handled.isEmpty())

        requests.processEligible(endCooldownTick - 1) { id -> handled.add(id) }
        assertTrue(handled.isEmpty())

        requests.processEligible(endCooldownTick) { id -> handled.add(id) }
        assertEquals(setOf(id1, id2), handled)
    }
}
