package io.github.thatdutchguy.torched.client

import io.github.thatdutchguy.torched.client.gui.vanillaTorchThrowingServerDisabledWarningVisible
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private typealias Permissions = VanillaTorchThrowingPermissions

class AllowVanillaTorchThrowing {
    @Test
    fun `when server disallows rejects`() {
        assertFalse(allowVanillaTorchThrowing(Permissions(serverAllowed = false, clientAllowed = true)))
        assertFalse(allowVanillaTorchThrowing(Permissions(serverAllowed = false, clientAllowed = false)))
    }

    @Test
    fun `when server permission is unknown rejects`() {
        assertFalse(allowVanillaTorchThrowing(Permissions(serverAllowed = null, clientAllowed = true)))
        assertFalse(allowVanillaTorchThrowing(Permissions(serverAllowed = null, clientAllowed = false)))
    }

    @Test
    fun `when server allows follows client config`() {
        assertTrue(allowVanillaTorchThrowing(Permissions(serverAllowed = true, clientAllowed = true)))
        assertFalse(allowVanillaTorchThrowing(Permissions(serverAllowed = true, clientAllowed = false)))
    }
}

class VanillaTorchThrowingServerDisabledWarningVisible {
    @Test
    fun `when connected to integrated server hides warning`() {
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = true, permissions = Permissions(serverAllowed = false, clientAllowed = true)
            )
        )
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = true, permissions = Permissions(serverAllowed = false, clientAllowed = false)
            )
        )
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = true, permissions = Permissions(serverAllowed = null, clientAllowed = true)
            )
        )
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = true, permissions = Permissions(serverAllowed = null, clientAllowed = false)
            )
        )
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = true, permissions = Permissions(serverAllowed = true, clientAllowed = true)
            )
        )
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = true, permissions = Permissions(serverAllowed = true, clientAllowed = false)
            )
        )
    }

    @Test
    fun `when connected to remote server with policy disabled and locally disabled hides warning`() {
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = false, permissions = Permissions(serverAllowed = false, clientAllowed = false)
            )
        )
    }

    @Test
    fun `when connected to remote server with policy disabled and locally enabled shows warning`() {
        assertTrue(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = false, permissions = Permissions(serverAllowed = false, clientAllowed = true)
            )
        )
    }

    @Test
    fun `when connected to remote server with policy enabled hides warning`() {
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = false, permissions = Permissions(serverAllowed = true, clientAllowed = true)
            )
        )
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = false, permissions = Permissions(serverAllowed = true, clientAllowed = false)
            )
        )
    }

    @Test
    fun `when not connected to any server hides warning`() {
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = false, permissions = Permissions(serverAllowed = null, clientAllowed = true)
            )
        )
        assertFalse(
            vanillaTorchThrowingServerDisabledWarningVisible(
                integratedServer = false, permissions = Permissions(serverAllowed = null, clientAllowed = false)
            )
        )
    }
}

