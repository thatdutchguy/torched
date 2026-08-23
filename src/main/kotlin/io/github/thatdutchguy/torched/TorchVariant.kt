package io.github.thatdutchguy.torched

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

enum class TorchVariant(
    val variantName: String,
    val stickyName: String,
    val displayName: String,
    val sourceItem: Item,
    val standingBlock: Block,
    val wallBlock: Block,
    val extinguishedByWater: Boolean,
    val creativeTab: ResourceKey<CreativeModeTab>,
) {
    TORCH(
        variantName = "torch",
        stickyName = "sticky_torch",
        displayName = "Sticky Torch",
        sourceItem = Items.TORCH,
        standingBlock = Blocks.TORCH,
        wallBlock = Blocks.WALL_TORCH,
        extinguishedByWater = true,
        creativeTab = CreativeModeTabs.FUNCTIONAL_BLOCKS,
    ),
    SOUL(
        variantName = "soul_torch",
        stickyName = "sticky_soul_torch",
        displayName = "Sticky Soul Torch",
        sourceItem = Items.SOUL_TORCH,
        standingBlock = Blocks.SOUL_TORCH,
        wallBlock = Blocks.SOUL_WALL_TORCH,
        extinguishedByWater = true,
        creativeTab = CreativeModeTabs.FUNCTIONAL_BLOCKS,
    ),
    REDSTONE(
        variantName = "redstone_torch",
        stickyName = "sticky_redstone_torch",
        displayName = "Sticky Redstone Torch",
        sourceItem = Items.REDSTONE_TORCH,
        standingBlock = Blocks.REDSTONE_TORCH,
        wallBlock = Blocks.REDSTONE_WALL_TORCH,
        extinguishedByWater = false,
        creativeTab = CreativeModeTabs.REDSTONE_BLOCKS,
    ),
    COPPER(
        variantName = "copper_torch",
        stickyName = "sticky_copper_torch",
        displayName = "Sticky Copper Torch",
        sourceItem = Items.COPPER_TORCH,
        standingBlock = Blocks.COPPER_TORCH,
        wallBlock = Blocks.COPPER_WALL_TORCH,
        extinguishedByWater = true,
        creativeTab = CreativeModeTabs.FUNCTIONAL_BLOCKS,
    );

    val sourceItemKey: ResourceKey<Item>
        get() = BuiltInRegistries.ITEM.getResourceKey(sourceItem).orElseThrow()

    val stickyItemKey: ResourceKey<Item>
        get() = ResourceKey.create(Registries.ITEM, Torched.id(stickyName))

    companion object {
        private val BY_SOURCE_ITEM: Map<Item, TorchVariant> by lazy {
            entries.associateBy { it.sourceItem }
        }

        fun ofSourceItem(item: Item): TorchVariant? = BY_SOURCE_ITEM[item]

        fun of(stack: ItemStack): TorchVariant? {
            val item = stack.item
            return (item as? ThrowableTorchItem)?.variant ?: ofSourceItem(item)
        }
    }
}
