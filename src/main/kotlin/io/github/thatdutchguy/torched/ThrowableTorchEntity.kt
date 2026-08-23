package io.github.thatdutchguy.torched

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

class ThrowableTorchEntity : ThrowableItemProjectile {
    constructor(type: EntityType<out ThrowableTorchEntity>, level: Level) : super(type, level)

    constructor(level: Level, owner: LivingEntity) : super(ModEntityTypes.THROWABLE_TORCH_ENTITY, level) {
        setOwner(owner)
        setPos(owner.x, owner.eyeY - 0.1, owner.z)
    }

    override fun getDefaultItem(): Item = ModItems.sticky(TorchVariant.TORCH)

    private val isSticky: Boolean
        get() = item.item is ThrowableTorchItem

    private val variant: TorchVariant?
        get() = TorchVariant.of(item)

    private val isExtinguishedByWater: Boolean
        get() = variant?.extinguishedByWater ?: true

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        if (level().isClientSide) return

        val hitFloor = hitResult.direction == Direction.UP
        val hitCeiling = hitResult.direction == Direction.DOWN
        val hitWall = !(hitFloor || hitCeiling)

        if (!(hitFloor || (isSticky && hitWall))) {
            handleImpact(hitResult, hitCeiling)
            return
        }

        fun blockStateForVariant(): BlockState? {
            val torch = variant ?: return null
            if (hitWall) {
                return torch.wallBlock.defaultBlockState().setValue(
                    BlockStateProperties.HORIZONTAL_FACING,
                    hitResult.direction
                )
            } else {
                return torch.standingBlock.defaultBlockState()
            }
        }

        fun blockStateForUnknown(): BlockState? {
            val blockItem = item.item as? BlockItem ?: return null
            return blockItem.block.defaultBlockState()
        }

        val blockState = blockStateForVariant() ?: blockStateForUnknown()
        val landingPos = hitResult.blockPos.relative(hitResult.direction)

        if (blockState == null) {
            dropAsItem(landingPos)
            discard()
            return
        }

        fun canPlace(): Boolean {
            if (!blockState.canSurvive(level(), landingPos)) return false
            if (!level().getBlockState(landingPos).canBeReplaced()) return false
            return true
        }

        fun isAuthorized(): Boolean {
            val player = getOwner() as? ServerPlayer ?: return false
            if (player.isSpectator) return false
            if (!player.mayUseItemAt(landingPos, hitResult.direction, item)) return false
            if (!level().mayInteract(player, landingPos)) return false
            return true
        }

        val inFluid = !level().getFluidState(landingPos).isEmpty

        if (!inFluid && isAuthorized() && canPlace()) {
            if (level().setBlockAndUpdate(landingPos, blockState)) {
                triggerPlacementEffects(landingPos)
            } else {
                dropAsItem(landingPos)
            }
        } else {
            if (inFluid && isExtinguishedByWater) {
                triggerExtinguishEffects(landingPos)
            }

            dropAsItem(landingPos)
        }

        discard()
    }

    private fun dropAsItem(pos: BlockPos) {
        level().addFreshEntity(
            ItemEntity(
                level(),
                pos.x + 0.5,
                pos.y + 0.5,
                pos.z + 0.5,
                item.copy(),
            )
        )
    }

    private fun handleImpact(hitResult: BlockHitResult, hitCeiling: Boolean) {
        nudgeOutOfBlock(hitResult.direction)
        deltaMovement = when {
            hitCeiling -> Vec3.ZERO
            else -> Vec3(0.0, deltaMovement.y, 0.0)
        }
    }

    private fun nudgeOutOfBlock(direction: Direction) {
        setPos(
            x + direction.stepX * 0.1,
            y + direction.stepY * 0.1,
            z + direction.stepZ * 0.1,
        )
    }

    private fun triggerPlacementEffects(pos: BlockPos) {
        val soundEvent = when {
            isSticky -> SoundEvents.SLIME_BLOCK_PLACE
            else -> SoundEvents.WOOD_PLACE
        }

        level().playSound(
            null,
            pos.x + 0.5,
            pos.y + 0.5,
            pos.z + 0.5,
            soundEvent,
            SoundSource.NEUTRAL,
            0.5f,
            1.0f,
        )
    }

    private fun triggerExtinguishEffects(pos: BlockPos) {
        val px = pos.x + 0.5
        val py = pos.y + 0.5
        val pz = pos.z + 0.5

        level().playSound(
            null,
            px, py, pz,
            SoundEvents.FIRE_EXTINGUISH,
            SoundSource.NEUTRAL,
            0.7f,
            1.2f,
        )

        (level() as ServerLevel).sendParticles(
            ParticleTypes.LARGE_SMOKE,
            px, py, pz,
            6,
            0.1,
            0.1,
            0.1,
            0.02,
        )
    }
}
