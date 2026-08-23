package io.github.thatdutchguy.torched.client.datagen

import io.github.thatdutchguy.torched.ModItems
import io.github.thatdutchguy.torched.TorchVariant
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.ShapelessRecipeBuilder
import net.minecraft.world.item.Items
import java.util.concurrent.CompletableFuture

class ModRecipeProvider(
    output: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricRecipeProvider(output, registriesFuture) {

    override fun createRecipeProvider(
        registries: HolderLookup.Provider,
        exporter: RecipeOutput,
    ): RecipeProvider = object : RecipeProvider(registries, exporter) {
        private val itemLookup = registries.lookupOrThrow(Registries.ITEM)

        override fun buildRecipes() {
            for (variant in TorchVariant.entries) {
                ShapelessRecipeBuilder
                    .shapeless(itemLookup, recipeCategoryOf(variant), ModItems.sticky(variant))
                    .requires(variant.sourceItem)
                    .requires(Items.SLIME_BALL)
                    .unlockedBy("has_${variant.variantName}", has(variant.sourceItem))
                    .save(exporter)
            }
        }
    }

    override fun getName(): String = "Torched Recipes"

    private companion object {
        fun recipeCategoryOf(variant: TorchVariant): RecipeCategory = when (variant) {
            TorchVariant.REDSTONE -> RecipeCategory.REDSTONE
            else -> RecipeCategory.DECORATIONS
        }
    }
}
