package io.github.thatdutchguy.torched

import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object ModTags {
    val THROWABLE_TORCHES: TagKey<Item> = TagKey.create(Registries.ITEM, Torched.id("throwable_torches"))
}
