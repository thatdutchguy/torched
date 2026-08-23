package io.github.thatdutchguy.torched.client.datagen

import io.github.thatdutchguy.torched.ModItems
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.model.ModelTemplates

class ModModelProvider(output: FabricPackOutput) : FabricModelProvider(output) {

    override fun generateBlockStateModels(generators: BlockModelGenerators) {
        // thrown torches place vanilla blocks, so nothing to generate
    }

    override fun generateItemModels(generators: ItemModelGenerators) {
        for (item in ModItems.allSticky) {
            generators.generateFlatItem(item, ModelTemplates.FLAT_ITEM)
        }
    }
}
