package io.github.thatdutchguy.torched.client.datagen

import com.google.gson.JsonObject
import io.github.thatdutchguy.torched.TorchVariant
import io.github.thatdutchguy.torched.Torched
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import java.util.concurrent.CompletableFuture

class DynamicLightsProvider(output: FabricPackOutput) : DataProvider {
    private val pathProvider: PackOutput.PathProvider =
        output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "dynamiclights/item")

    override fun run(cache: CachedOutput): CompletableFuture<*> {
        val futures = TorchVariant.entries.map { variant ->
            val json = JsonObject().apply {
                add("match", JsonObject().apply {
                    addProperty("items", "${Torched.MOD_ID}:${variant.stickyName}")
                })
                add("luminance", JsonObject().apply {
                    addProperty("type", "block")
                    addProperty("block", "minecraft:${variant.variantName}")
                })
                addProperty("water_sensitive", variant.extinguishedByWater)
            }

            DataProvider.saveStable(cache, json, pathProvider.json(Torched.id(variant.stickyName)))
        }

        return CompletableFuture.allOf(*futures.toTypedArray())
    }

    override fun getName(): String = "Torched Dynamic Lights"
}
