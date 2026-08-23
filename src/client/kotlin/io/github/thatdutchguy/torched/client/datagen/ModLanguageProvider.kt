package io.github.thatdutchguy.torched.client.datagen

import io.github.thatdutchguy.torched.ModItems
import io.github.thatdutchguy.torched.TorchVariant
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class ModLanguageProvider(
    output: FabricPackOutput,
    registryLookup: CompletableFuture<HolderLookup.Provider>,
) : FabricLanguageProvider(output, registryLookup) {

    override fun generateTranslations(registryLookup: HolderLookup.Provider, builder: TranslationBuilder) {
        for (variant in TorchVariant.entries) {
            builder.add(ModItems.sticky(variant), variant.displayName)
        }

        builder.add("key.torched.throw_torch", "Throw Torch")

        builder.add("options.torched.title", "Torched Settings")

        builder.add("options.torched.throw_on_use", "Throw on use (Right-Click)")
        builder.add(
            "options.torched.throw_on_use.tooltip",
            "Right-clicking with nothing in range throws the torch you are holding. " +
                    "Turn this off to throw only with the throw key.",
        )

        builder.add("options.torched.throw_vanilla_torches", "Throw Plain Torches")
        builder.add(
            "options.torched.throw_vanilla_torches.tooltip",
            "Whether ordinary torches can be thrown, or only sticky ones. " +
                    "Server configuration will override this setting.",
        )

        builder.add("tag.item.torched.throwable_torches", "Throwable Torches")
    }
}
