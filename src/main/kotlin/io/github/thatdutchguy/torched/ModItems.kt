package io.github.thatdutchguy.torched

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item

object ModItems {
    private val STICKY_TORCHES: Map<TorchVariant, ThrowableTorchItem> = TorchVariant.entries.associateWith { variant ->
        register(
            name = variant.stickyName,
            itemFactory = { properties -> ThrowableTorchItem(properties, variant) },
            settings = Item.Properties(),
        )
    }

    fun sticky(variant: TorchVariant): ThrowableTorchItem = STICKY_TORCHES.getValue(variant)

    val allSticky: Collection<ThrowableTorchItem>
        get() = STICKY_TORCHES.values

    fun <T : Item> register(name: String, itemFactory: (Item.Properties) -> T, settings: Item.Properties): T {
        val itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Torched.MOD_ID, name))
        val item = itemFactory(settings.setId(itemKey))
        Registry.register(BuiltInRegistries.ITEM, itemKey, item)
        return item
    }

    fun initialize() {}
}
