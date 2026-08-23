package io.github.thatdutchguy.torched

import net.minecraft.core.Direction
import net.minecraft.world.item.StandingAndWallBlockItem

class ThrowableTorchItem(properties: Properties, val variant: TorchVariant) :
    StandingAndWallBlockItem(variant.standingBlock, variant.wallBlock, Direction.DOWN, properties) {
}
