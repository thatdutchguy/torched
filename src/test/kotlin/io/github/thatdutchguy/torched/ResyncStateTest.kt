package io.github.thatdutchguy.torched

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ResyncStateTest {
    @Test
    fun `request() marks pending`() {
        val state = ResyncState()
        assertFalse(state.isPending)
        state.request()
        assertTrue(state.isPending)
    }

    @Test
    fun `markSent() clears pending`() {
        val state = ResyncState()
        state.request()
        state.markSent(10)
        assertFalse(state.isPending)
    }

    @Test
    fun `markSent() sets lastSentTick`() {
        val state = ResyncState()
        state.request()
        assertNull(state.lastSentTick)
        state.markSent(10)
        assertEquals(10, state.lastSentTick)
    }

    @Test
    fun `isEligible() returns false when not pending`() {
        val state = ResyncState()
        state.request()
        state.markSent(0)
        assertFalse(state.isPending)
        assertFalse(state.isEligible(30, cooldownTicks = 20))
    }

    @Test
    fun `isEligible is true when lastSentTick is null`() {
        val state = ResyncState()
        state.request()
        assertEquals(null, state.lastSentTick)
        assertTrue(state.isEligible(0, cooldownTicks = 20))
    }

    @Test
    fun `isEligible honors cooldown`() {
        val state = ResyncState()
        state.markSent(0)
        state.request()
        assertEquals(0, state.lastSentTick)
        assertFalse(state.isEligible(0, cooldownTicks = 20))
        assertFalse(state.isEligible(10, cooldownTicks = 20))
        assertFalse(state.isEligible(19, cooldownTicks = 20))
        assertTrue(state.isEligible(20, cooldownTicks = 20))
    }
}
