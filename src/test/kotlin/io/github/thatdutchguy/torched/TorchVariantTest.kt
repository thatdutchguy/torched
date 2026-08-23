package io.github.thatdutchguy.torched

import net.minecraft.SharedConstants
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.Bootstrap
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TorchVariantTest {
    @BeforeEach
    fun bootstrap() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `variant define source item resource key`() {
        for (entry in TorchVariant.entries) {
            val identifier = entry.sourceItemKey.identifier().toString()
            assertEquals(deriveVanillaItemId(entry), identifier)
        }
    }

    @Test
    fun `variant define sticky item resource key`() {
        for (entry in TorchVariant.entries) {
            val identifier = entry.stickyItemKey.identifier().toString()
            assertEquals(deriveStickyItemId(entry), identifier)
        }
    }

    @Test
    fun `variant fields are unique`() {
        val fields: MutableSet<String> = mutableSetOf()
        val dupes: MutableSet<String> = mutableSetOf()
        for (selector in listOf(TorchVariant::variantName, TorchVariant::stickyName)) {
            val dupes = TorchVariant.entries.groupBy(selector).filterValues { it.size > 1 }
            assertTrue(dupes.isEmpty(), "duplicate ${selector.name}: $dupes")
        }
        fun add(field: String) { fields.add(field) || dupes.add(field) }
        for (entry in TorchVariant.entries) {
            add(entry.variantName)
            add(entry.stickyName)
        }
        assertTrue(dupes.isEmpty(), "duplicates: $dupes")
    }

    @Test
    fun `variant blocks are unique`() {
        // NOTE: Variant currently map 1:1 to blocks, though it's by convention, and nothing in the design requires it.
        // If this convention changes, update the test accordingly.
        val blocks: MutableSet<Block> = mutableSetOf()
        val dupes: MutableSet<Block> = mutableSetOf()
        fun add(block: Block) { blocks.add(block) || dupes.add(block) }
        for (entry in TorchVariant.entries) {
            add(entry.standingBlock)
            add(entry.wallBlock)
        }
        assertTrue(dupes.isEmpty(), "duplicates: $dupes")
    }

    @Test
    fun `variant resolve from source item`() {
        for (entry in TorchVariant.entries) {
            val variant = TorchVariant.ofSourceItem(entry.sourceItem)
            assertEquals(entry, variant)
        }
    }

    @Test
    fun `variant standing blocks have wall block counterparts`() {
        for (entry in TorchVariant.entries) {
            val blocks = BuiltInRegistries.BLOCK
            val standingBlockName = deriveStandingBlockName(entry)
            val wallBlockName = deriveWallBlockName(entry)
            assertEquals(standingBlockName, blocks.getKey(entry.standingBlock).toString())
            assertEquals(wallBlockName, blocks.getKey(entry.wallBlock).toString())
        }
    }

    @Test
    fun `variant wall blocks are HORIZONTAL_FACING`() {
        for (entry in TorchVariant.entries) {
            val state = entry.wallBlock.defaultBlockState()
            assertTrue(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
        }
    }

    @Test
    fun `variant standing blocks are not HORIZONTAL_FACING`() {
        for (entry in TorchVariant.entries) {
            val state = entry.standingBlock.defaultBlockState()
            assertFalse(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
        }
    }

    // NOTE: derivations based on naming convention
    private fun deriveVanillaItemId(variant: TorchVariant) = "minecraft:${variant.variantName}"
    private fun deriveStickyItemId(variant: TorchVariant) = "torched:sticky_${variant.variantName}"
    private fun deriveStandingBlockName(variant: TorchVariant) = deriveVanillaItemId(variant)
    private fun deriveWallBlockName(variant: TorchVariant): String {
        val standingBlockName = deriveStandingBlockName(variant)
        val wallBlockName = standingBlockName.replaceFirst("torch", "wall_torch")
        if  (standingBlockName == wallBlockName) {
            error("failed to derive wall block name: $wallBlockName")
        }
        return wallBlockName
    }
}

