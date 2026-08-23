package io.github.thatdutchguy.torched.client.datagen

import io.github.thatdutchguy.torched.ModTags
import io.github.thatdutchguy.torched.TorchVariant
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class ModItemTagProvider(
    output: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricTagsProvider.ItemTagsProvider(output, registriesFuture) {

    override fun addTags(registries: HolderLookup.Provider) {
        val appender = builder(ModTags.THROWABLE_TORCHES)

        for (variant in TorchVariant.entries) {
            appender.add(variant.sourceItemKey)
            appender.add(variant.stickyItemKey)
        }
    }
}
