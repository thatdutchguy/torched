package io.github.thatdutchguy.torched.client

import io.github.thatdutchguy.torched.client.datagen.DynamicLightsProvider
import io.github.thatdutchguy.torched.client.datagen.ModItemTagProvider
import io.github.thatdutchguy.torched.client.datagen.ModLanguageProvider
import io.github.thatdutchguy.torched.client.datagen.ModModelProvider
import io.github.thatdutchguy.torched.client.datagen.ModRecipeProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object TorchedDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::ModRecipeProvider)
        pack.addProvider(::ModItemTagProvider)
        pack.addProvider(::ModModelProvider)
        pack.addProvider(::ModLanguageProvider)
        pack.addProvider(::DynamicLightsProvider)
    }
}
