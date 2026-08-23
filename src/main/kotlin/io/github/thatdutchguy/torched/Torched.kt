package io.github.thatdutchguy.torched

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory

object Torched : ModInitializer {
    const val MOD_ID: String = "torched"
    val logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        val version = FabricLoader.getInstance().getModContainer(MOD_ID).map { it.metadata.version.friendlyString }
            .orElse("Unknown version")
        logger.info("Initializing Throwable Torch Mod $version")

        TorchedConfig.load()

        ModItems.initialize()
        ModEntityTypes.initialize()
        ServerThrowRateLimiter.initialize()
        ServerInventoryResync.initialize()

        registerCreativeTabEntries()
        registerNetworking()
    }

    private fun registerCreativeTabEntries() {
        for (variant in TorchVariant.entries) {
            CreativeModeTabEvents.modifyOutputEvent(variant.creativeTab).register { output ->
                output.insertAfter(variant.sourceItem, ModItems.sticky(variant))
            }
        }
    }

    private fun registerNetworking() {
        PayloadTypeRegistry.serverboundPlay().register(ThrowTorchPayload.TYPE, ThrowTorchPayload.STREAM_CODEC)
        ServerPlayNetworking.registerGlobalReceiver(ThrowTorchPayload.TYPE) { payload, context ->
            val player = context.player()
            val currentTick = context.server().tickCount
            if (!ServerThrowRateLimiter.tryConsume(player.uuid, currentTick)) {
                ServerInventoryResync.request(player)
                return@registerGlobalReceiver
            }
            if (!TorchThrowing.throwTorch(player, payload.hand)) {
                ServerInventoryResync.request(player)
            }
        }
    }

    fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)
}
