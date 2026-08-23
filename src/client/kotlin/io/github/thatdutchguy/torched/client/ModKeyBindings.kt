package io.github.thatdutchguy.torched.client

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping

object ModKeyBindings {
    val THROW_TORCH: KeyMapping = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.torched.throw_torch",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            KeyMapping.Category.GAMEPLAY,
        )
    )

    fun initialize() {}
}
