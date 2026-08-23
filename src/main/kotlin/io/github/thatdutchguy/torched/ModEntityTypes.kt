package io.github.thatdutchguy.torched

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

object ModEntityTypes {
    val THROWABLE_TORCH_ENTITY = register(
        "throwable_torch_entity", EntityType.Builder.of(::ThrowableTorchEntity, MobCategory.MISC).sized(0.25f, 0.25f)
    )

    private fun <T : Entity> register(name: String, builder: EntityType.Builder<T>): EntityType<T> {
        val key = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Torched.MOD_ID, name)
        )
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key))
    }

    fun initialize() {}
}
