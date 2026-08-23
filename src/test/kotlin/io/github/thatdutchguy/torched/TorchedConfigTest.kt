package io.github.thatdutchguy.torched

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TorchedConfigTest {
    @Test
    fun `parses config`() {
        assertConfig(
            """{"throwOnUse":false, "throwVanillaTorches":false}""",
            throwOnUse = false,
            throwVanillaTorches = false
        )
    }

    @Test
    fun `missing key falls back to default`() {
        assertConfig(
            """{"throwOnUse":false}""",
            throwOnUse = false,
            throwVanillaTorches = true
        )
        assertConfig(
            """{"throwVanillaTorches":false}""",
            throwOnUse = true,
            throwVanillaTorches = false
        )
        assertConfig(
            "{}",
            throwOnUse = true,
            throwVanillaTorches = true
        )
    }

    @Test
    fun `null key falls back to default`() {
        assertConfig(
            """{"throwOnUse":null, "throwVanillaTorches":null}""",
            throwOnUse = true,
            throwVanillaTorches = true
        )
        assertConfig(
            """{"throwOnUse":false, "throwVanillaTorches":null}""",
            throwOnUse = false,
            throwVanillaTorches = true
        )
        assertConfig(
            """{"throwOnUse":null, "throwVanillaTorches":false}""",
            throwOnUse = true,
            throwVanillaTorches = false
        )
    }

    @Test
    fun `null config falls back to defaults`() {
        assertConfig(
            null,
            throwOnUse = true,
            throwVanillaTorches = true
        )
    }

    @Test
    fun `invalid config falls back to defaults`() {
        assertConfig(
            """INVALID""",
            throwOnUse = true,
            throwVanillaTorches = true
        )
        assertConfig(
            """{"throwOnUse":"no", "throwVanillaTorches":0}""",
            throwOnUse = true,
            throwVanillaTorches = true
        )
    }

    private fun assertConfig(json: String?, throwOnUse: Boolean, throwVanillaTorches: Boolean) {
        val config = TorchedConfig.parse(json)
        assertEquals(throwOnUse, config.throwOnUse, "throwOnUse for $json")
        assertEquals(throwVanillaTorches, config.throwVanillaTorches, "throwVanillaTorches for $json")
    }
}
