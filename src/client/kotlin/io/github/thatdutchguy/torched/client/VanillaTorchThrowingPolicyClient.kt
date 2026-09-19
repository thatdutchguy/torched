package io.github.thatdutchguy.torched.client

import io.github.thatdutchguy.torched.TorchedConfig
import io.github.thatdutchguy.torched.VanillaTorchThrowingPolicyPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking


object VanillaTorchThrowingPolicyClient {
    private var serverPolicy: Boolean? = null

    val permissions get() = VanillaTorchThrowingPermissions(
        serverAllowed = serverPolicy,
        clientAllowed = TorchedConfig.data.throwVanillaTorches
    )

    fun initialize() {
        ClientPlayNetworking.registerGlobalReceiver(VanillaTorchThrowingPolicyPayload.TYPE) { payload, _ ->
            serverPolicy = payload.allowed
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> serverPolicy = null }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> serverPolicy = null }
    }
}

data class VanillaTorchThrowingPermissions(
    val serverAllowed: Boolean?,
    val clientAllowed: Boolean
)

fun allowVanillaTorchThrowing(permissions: VanillaTorchThrowingPermissions) =
    (permissions.serverAllowed == true) && permissions.clientAllowed
