package io.github.thatdutchguy.torched

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

object ServerInventoryResync {
    private val requests = ResyncRequests()

    fun initialize() {
        ServerTickEvents.END_SERVER_TICK.register { server -> sendEligible(server, server.tickCount) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> clear(handler.player) }
    }

    fun request(player: ServerPlayer) {
        requests.request(player.uuid)
    }

    private fun clear(player: ServerPlayer) {
        requests.clear(player.uuid)
    }

    private fun sendEligible(server: MinecraftServer, tickCount: Int) {
        requests.processEligible(tickCount) { playerId -> send(server, playerId) }
    }

    private fun send(server: MinecraftServer, playerId: UUID): Boolean {
        val player = server.playerList.getPlayer(playerId) ?: return false
        player.containerMenu.sendAllDataToRemote()
        return true
    }
}

internal class ResyncRequests(
    private val cooldownTicks: Int = 20
) {
    private val states = mutableMapOf<UUID, ResyncState>()

    fun request(playerId: UUID) {
        states.getOrPut(playerId, ::ResyncState).request()
    }

    fun clear(playerId: UUID) {
        states.remove(playerId)
    }

    fun processEligible(tickCount: Int, trySend: (playerId: UUID) -> Boolean) {
        for ((playerId, state) in states) {
            if (state.isEligible(tickCount, cooldownTicks) && trySend(playerId)) {
                state.markSent(tickCount)
            }
        }
    }
}

internal class ResyncState {
    var isPending: Boolean = false
        private set

    var lastSentTick: Int? = null
        private set

    fun request() {
        isPending = true
    }

    fun markSent(tickCount: Int) {
        isPending = false
        lastSentTick = tickCount
    }

    fun isEligible(tickCount: Int, cooldownTicks: Int): Boolean {
        if (!isPending) return false
        val lastSent = lastSentTick ?: return true
        return tickCount - lastSent >= cooldownTicks
    }
}
