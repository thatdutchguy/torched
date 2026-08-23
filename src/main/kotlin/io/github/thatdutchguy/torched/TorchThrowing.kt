package io.github.thatdutchguy.torched

import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

object TorchThrowing {
    private val throwPower = 1.5f // same as snowballs
    private val throwUncertainty = 0.0f
    private val throwOffset = 0.0f

    fun canThrow(stack: ItemStack): Boolean = canThrow(stack, TorchedConfig.data.throwVanillaTorches)

    fun canThrow(stack: ItemStack, allowVanilla: Boolean): Boolean {
        if (stack.isEmpty) return false
        if (stack.item is ThrowableTorchItem) return true
        if (!allowVanilla) return false
        return stack.typeHolder().`is`(ModTags.THROWABLE_TORCHES)
    }

    fun throwTorch(player: Player, hand: InteractionHand): Boolean {
        val level = player.level()
        if (level.isClientSide) return false
        if (!player.isAlive) return false
        if (player.isSpectator) return false

        val stack = player.getItemInHand(hand)
        if (!canThrow(stack)) return false

        val entity = ThrowableTorchEntity(level, player)
        entity.item = stack.copyWithCount(1)
        entity.shootFromRotation(player, player.xRot, player.yRot, throwOffset, throwPower, throwUncertainty)
        if (!level.addFreshEntity(entity)) return false

        playThrowSound(player)
        consumeTorch(player, stack)
        return true
    }

    fun applyThrowLocally(player: Player, hand: InteractionHand) {
        playThrowSound(player)
        consumeTorch(player, player.getItemInHand(hand))
    }

    private fun playThrowSound(player: Player) {
        // NOTE:
        //  - `except` is not null, because we already played the sound locally
        //  - all other arguments should be the same per event, as this method gets called locally and on
        //  the server, and should  trigger the same sound
        player.level().playSound(
            player, // NOTE: not null, because we already play it locally
            player.x,
            player.y,
            player.z,
            SoundEvents.SNOWBALL_THROW,
            SoundSource.NEUTRAL,
            0.5f,
            1.0f,
        )
    }

    private fun consumeTorch(player: Player, stack: ItemStack) {
        if (!player.abilities.instabuild) {
            stack.shrink(1)
        }
    }
}
