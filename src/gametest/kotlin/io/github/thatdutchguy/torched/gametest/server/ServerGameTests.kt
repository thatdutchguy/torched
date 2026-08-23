package io.github.thatdutchguy.torched.gametest.server

import io.github.thatdutchguy.torched.ModEntityTypes
import io.github.thatdutchguy.torched.ModItems
import io.github.thatdutchguy.torched.ThrowableTorchEntity
import io.github.thatdutchguy.torched.TorchThrowing
import io.github.thatdutchguy.torched.TorchVariant
import io.github.thatdutchguy.torched.Torched
import net.fabricmc.fabric.api.entity.FakePlayer
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.advancements.predicates.BlockPredicate
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.AdventureModePredicate
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.pattern.BlockInWorld
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.min

//region Wall Tests

class SticksToNorthWall : SticksToWall(Cardinal.NORTH)
class SticksToEastWall : SticksToWall(Cardinal.EAST)
class SticksToSouthWall : SticksToWall(Cardinal.SOUTH)
class SticksToWestWall : SticksToWall(Cardinal.WEST)

abstract class SticksToWall(val throwDirection: Cardinal) {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        val arena = attemptStickToWall(helper, variant, TorchType.STICKY, throwDirection)
        val wallPos = when (throwDirection) {
            Cardinal.NORTH -> throwOrigin.withZ(arena.inside.zMin)
            Cardinal.EAST -> throwOrigin.withX(arena.inside.xMax)
            Cardinal.SOUTH -> throwOrigin.withZ(arena.inside.zMax)
            Cardinal.WEST -> throwOrigin.withX(arena.inside.xMin)
        }

        helper.succeedWhen {
            helper.assertBlockPresent(variant.wallBlock, wallPos)
            helper.assertBlockProperty(wallPos, BlockStateProperties.HORIZONTAL_FACING, throwDirection.opposite)
        }
    }
}

class BouncesOffWall {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        val arena = attemptStickToWall(helper, variant, TorchType.VANILLA)
        val wallPos = throwOrigin.withZ(arena.inside.zMin)
        val floorPos = wallPos.withY(arena.inside.yMin)
        helper.runAfterDelay(20) {
            helper.assertBlockNotPresent(variant.wallBlock, wallPos)
            helper.succeedWhenBlockPresent(variant.standingBlock, floorPos)
        }
    }
}

private fun attemptStickToWall(
    helper: GameTestHelper,
    variant: TorchVariant,
    type: TorchType,
    throwDirection: Cardinal = Cardinal.NORTH
): EnclosedBox {
    val arena = EnclosedBox().also { helper.applyArena(it) }
    val target = when (throwDirection) {
        Cardinal.NORTH -> throwOrigin.withZ(arena.box.zMin)
        Cardinal.EAST -> throwOrigin.withX(arena.box.xMax)
        Cardinal.SOUTH -> throwOrigin.withZ(arena.box.zMax)
        Cardinal.WEST -> throwOrigin.withX(arena.box.xMin)
    }
    throwItemAtTarget(helper, variant.itemOfType(type), throwOrigin.center, target.center)

    return arena
}

// endregion

class PlacesVanillaOnFloor : PlacesOnFloor(TorchType.VANILLA)
class PlacesStickyOnFloor : PlacesOnFloor(TorchType.STICKY)

abstract class PlacesOnFloor(val torchType: TorchType) {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withY(arena.box.yMin)
        val check = target.withY(arena.inside.yMin)
        throwItemAtTarget(helper, variant.itemOfType(torchType), throwOrigin.center, target.center)
        helper.succeedWhenBlockPresent(variant.standingBlock, check)
    }
}

class BouncesVanillaOffCeiling : BouncesOffCeiling(TorchType.VANILLA)
class BouncesStickyOffCeiling : BouncesOffCeiling(TorchType.STICKY)

abstract class BouncesOffCeiling(val torchType: TorchType) {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withY(arena.box.yMax)
        val checkStanding = target.withY(arena.inside.yMin)
        val checkAir = target.withY(arena.inside.yMax)

        val entity = throwItemAtTarget(helper, variant.itemOfType(torchType), throwOrigin.center, target.center)
        var peakY = Double.NEGATIVE_INFINITY
        helper.onEachTick {
            if (!entity.isRemoved) {
                val currentY = helper.relativeVec(entity.position()).y
                peakY = maxOf(peakY, currentY)
            }
        }

        val touchedCeiling = {
            val nearCeilingMargin = 0.5
            val ceilingY = target.y.toDouble()
            peakY in (ceilingY - nearCeilingMargin)..<ceilingY
        }

        helper.runAfterDelay(20) {
            if (!touchedCeiling()) helper.fail("torch never touched the ceiling (peaked at $peakY)")
            helper.assertBlockPresent(Blocks.AIR, checkAir)
            helper.succeedWhenBlockPresent(variant.standingBlock, checkStanding)
        }
    }
}

class DropsVanillaOnFluid : DropsOnFluid(TorchType.VANILLA)
class DropsStickyOnFluid : DropsOnFluid(TorchType.STICKY)

abstract class DropsOnFluid(val torchType: TorchType) {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        val arena = EnclosedBoxWithWater().also { helper.applyArena(it) }
        val item = variant.itemOfType(torchType)
        val target = throwOrigin.withY(arena.box.yMin)
        val check = target.withY(arena.inside.yMin)
        throwItemAtTarget(helper, item, throwOrigin.center, target.center)
        helper.runAfterDelay(20) {
            helper.assertBlockPresent(Blocks.WATER, check)
            helper.assertItemEntityPresent(item)
            helper.succeed()
        }
    }
}

class RedstonePlacedOnFloorStatus {
    @GameTest(maxTicks = 100)
    fun stickyUnlit(helper: GameTestHelper) = test(helper, TorchType.STICKY, Blocks.REDSTONE_BLOCK, expectLit = false)

    @GameTest(maxTicks = 100)
    fun vanillaUnlit(helper: GameTestHelper) = test(helper, TorchType.VANILLA, Blocks.REDSTONE_BLOCK, expectLit = false)

    @GameTest(maxTicks = 100)
    fun stickyLit(helper: GameTestHelper) = test(helper, TorchType.STICKY, Blocks.STONE, expectLit = true)

    @GameTest(maxTicks = 100)
    fun vanillaLit(helper: GameTestHelper) = test(helper, TorchType.VANILLA, Blocks.STONE, expectLit = true)

    private fun test(helper: GameTestHelper, type: TorchType, block: Block, expectLit: Boolean) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withY(arena.box.yMin)
        val check = target.withY(arena.inside.yMin)
        helper.setBlock(target, block)

        val variant = TorchVariant.REDSTONE
        throwItemAtTarget(helper, variant.itemOfType(type), throwOrigin.center, target.center)

        helper.runAfterDelay(20) {
            helper.assertBlockPresent(Blocks.REDSTONE_TORCH, check)
            helper.assertBlockProperty(check, BlockStateProperties.LIT, expectLit)
            helper.succeed()
        }
    }
}

class RedstonePlacedOnWallStatus {
    @GameTest(maxTicks = 100)
    fun stickyUnlit(helper: GameTestHelper) = test(helper, Blocks.REDSTONE_BLOCK, expectLit = false)

    @GameTest(maxTicks = 100)
    fun stickyLit(helper: GameTestHelper) = test(helper, Blocks.STONE, expectLit = true)

    private fun test(helper: GameTestHelper, block: Block, expectLit: Boolean) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withZ(arena.box.zMin)
        val check = target.withZ(arena.inside.zMin)
        val variant = TorchVariant.REDSTONE
        val item = variant.itemOfType(TorchType.STICKY)
        helper.setBlock(target, block)
        throwItemAtTarget(helper, item, throwOrigin.center, target.center)

        helper.runAfterDelay(20) {
            helper.assertBlockPresent(Blocks.REDSTONE_WALL_TORCH, check)
            helper.assertBlockProperty(check, BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
            helper.assertBlockProperty(check, BlockStateProperties.LIT, expectLit)
            helper.succeed()
        }
    }
}

class UnsurvivableFloorDropsVanilla : UnsurvivableFloorDrops(TorchType.VANILLA)
class UnsurvivableFloorDropsSticky : UnsurvivableFloorDrops(TorchType.STICKY)

abstract class UnsurvivableFloorDrops(val torchType: TorchType) {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withY(arena.box.yMin)
        val check = target.withY(arena.inside.yMin)
        val leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true)
        helper.setBlock(target, leaves)

        val item = variant.itemOfType(torchType)
        throwItemAtTarget(helper, item, throwOrigin.center, target.center)

        helper.runAfterDelay(20) {
            helper.assertBlockPresent(Blocks.AIR, check)
            helper.assertItemEntityPresent(item, check, 1.0)
            helper.succeed()
        }
    }
}

class DropsStickyOnUnsurvivableWall {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withZ(arena.box.zMin)
        val checkAir = target.withZ(arena.inside.zMin)
        val checkItem = checkAir.withY(arena.inside.yMin)
        val leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true)
        helper.setBlock(target, leaves)

        val item = variant.itemOfType(TorchType.STICKY)
        throwItemAtTarget(helper, item, throwOrigin.center, target.center)

        helper.runAfterDelay(20) {
            helper.assertBlockPresent(Blocks.AIR, checkAir)
            helper.assertItemEntityPresent(item, checkItem, 1.0)
            helper.succeed()
        }
    }
}

class HonorsBlockPlacementPermissions {
    @GameTest(maxTicks = 100)
    fun ownerNull(helper: GameTestHelper) = testOwner(helper, mode = null)

    @GameTest(maxTicks = 100)
    fun isSpectator(helper: GameTestHelper) = testOwner(helper, mode = GameType.SPECTATOR)

    @GameTest(maxTicks = 100)
    fun mayUse(helper: GameTestHelper) = testOwner(helper, mode = GameType.ADVENTURE)

    // NOTE: mayInteract not exercised here

    private fun testOwner(helper: GameTestHelper, mode: GameType?) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withY(arena.box.yMin)
        val check = target.withY(arena.inside.yMin)
        val item = TorchVariant.TORCH.itemOfType(TorchType.VANILLA)
        val player = mode?.let { gameType ->
            helper.makePlayerAt(playerOrigin, gameType)
        }

        throwItemAtTarget(helper, item, throwOrigin.center, target.center, owner = player)

        helper.runAfterDelay(20) {
            helper.assertBlockPresent(Blocks.AIR, check)
            helper.assertItemEntityPresent(item, check, 1.0)
            helper.succeed()
        }
    }

    @GameTest(maxTicks = 100)
    fun testAdventureWithCanPlaceOn(helper: GameTestHelper) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withY(arena.box.yMin)
        val check = target.withY(arena.inside.yMin)
        val variant = TorchVariant.TORCH
        val item = variant.itemOfType(TorchType.VANILLA)
        val player = helper.makePlayerAt(playerOrigin, GameType.ADVENTURE)

        val targetBlock = helper.getBlockState(target).block
        val predicate = BlockPredicate.Builder.block().of(BuiltInRegistries.BLOCK, targetBlock).build()
        val stack = ItemStack(item)
        stack.set(DataComponents.CAN_PLACE_ON, AdventureModePredicate(listOf(predicate)))

        if (!stack.canPlaceOnBlockInAdventureMode(BlockInWorld(helper.level, helper.absolutePos(target), false))) {
            helper.fail("setting up block permissions failed")
        }

        throwStackAtTarget(helper, stack, throwOrigin.center, target.center, owner = player)

        helper.runAfterDelay(20) {
            helper.assertBlockPresent(variant.standingBlock, check)
            helper.assertItemEntityNotPresent(item)
            helper.succeed()
        }
    }
}

class ReplacesReplaceableVanilla : BlockReplacement(TorchType.VANILLA, Blocks.SHORT_GRASS, expectReplace = true)
class ReplacesReplaceableSticky : BlockReplacement(TorchType.STICKY, Blocks.SHORT_GRASS, expectReplace = true)
class RefusesNonReplaceableVanilla :
    BlockReplacement(TorchType.VANILLA, Blocks.STONE_PRESSURE_PLATE, expectReplace = false)

class RefusesNonReplaceableSticky :
    BlockReplacement(TorchType.STICKY, Blocks.STONE_PRESSURE_PLATE, expectReplace = false)

abstract class BlockReplacement(val torchType: TorchType, val block: Block, val expectReplace: Boolean) {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        val target = throwOrigin.withY(arena.inside.yMin)
        helper.setBlock(target, block)

        val item = variant.itemOfType(torchType)
        helper.assertBlockPresent(block, target)
        throwItemAtTarget(helper, item, throwOrigin.center, target.center)

        helper.runAfterDelay(20) {
            if (expectReplace) {
                helper.assertBlockPresent(variant.standingBlock, target)
            } else {
                helper.assertBlockNotPresent(variant.standingBlock, target)
                helper.assertItemEntityPresent(item, target, 1.0)
            }

            helper.succeed()
        }
    }
}

class FailedPlacementDropsItem {
    @GameTest(maxTicks = 100)
    fun test(helper: GameTestHelper) {
        // NOTE: this test bypasses some of the regular helper methods and assertions, because they try to
        //  keep things inside the build limits, and this test is for a literal edge case
        val levelMaxY = helper.level.maxY
        val target = helper.absolutePos(BlockPos(3, 0, 3)).withY(levelMaxY)
        val check = target.withY(levelMaxY + 1)

        val platformBlock = Blocks.STONE.defaultBlockState()
        for (x in target.x - 2..target.x + 2) {
            for (z in target.z - 2..target.z + 2) {
                helper.level.setBlock(BlockPos(x, target.y, z), platformBlock, Block.UPDATE_ALL)
            }
        }

        if (helper.level.getBlockState(target).block != platformBlock.block) {
            helper.fail("failed to place block at world ceiling")
        }

        val origin = target.withY(levelMaxY + 4)
        val variant = TorchVariant.TORCH
        val type = TorchType.VANILLA
        val item = variant.itemOfType(type)
        val stack = ItemStack(item)
        val player = helper.makeMockServerPlayer(GameType.SURVIVAL)

        val dir = target.center.subtract(origin.center)
        throwStackInDirectionAbsolute(helper, stack, origin.center, dir, 0.7f, player)

        helper.succeedWhen {
            val state = helper.level.getBlockState(check)
            if (state.block != Blocks.VOID_AIR) {
                helper.fail("expected block at $check to be void_air, but was: $state")
            }
            val dropped = helper.level.hasEntities(
                EntityTypes.ITEM,
                AABB(check).inflate(2.5),
                { entity -> entity.isAlive && entity.item.`is`(item) }
            )
            if (!dropped) {
                helper.fail("expected item $item to be present at $check")
            }
        }
    }
}

class ThrowVanillaTorchMainHand : ThrowTorchFromHand(TorchType.VANILLA, InteractionHand.MAIN_HAND)
class ThrowVanillaTorchOffHand : ThrowTorchFromHand(TorchType.VANILLA, InteractionHand.OFF_HAND)
class ThrowStickyTorchMainHand : ThrowTorchFromHand(TorchType.STICKY, InteractionHand.MAIN_HAND)
class ThrowStickyTorchOffHand : ThrowTorchFromHand(TorchType.STICKY, InteractionHand.OFF_HAND)

abstract class ThrowTorchFromHand(val torchType: TorchType, val hand: InteractionHand) {
    @GameTest(maxTicks = 100)
    fun plain(helper: GameTestHelper) = test(helper, TorchVariant.TORCH)

    @GameTest(maxTicks = 100)
    fun copper(helper: GameTestHelper) = test(helper, TorchVariant.COPPER)

    @GameTest(maxTicks = 100)
    fun soul(helper: GameTestHelper) = test(helper, TorchVariant.SOUL)

    @GameTest(maxTicks = 100)
    fun redstone(helper: GameTestHelper) = test(helper, TorchVariant.REDSTONE)

    private fun test(helper: GameTestHelper, variant: TorchVariant) {
        helper.applyArena(EnclosedBox())
        val player = helper.makePlayerAt(playerOrigin)
        val createStack = { ItemStack(variant.itemOfType(torchType), 1) }
        player.setItemInHand(hand, createStack())
        player.setItemInHand(hand.otherHand, createStack())

        if (!TorchThrowing.throwTorch(player, hand))
            helper.fail("torch not thrown")

        if (!player.getItemInHand(hand).isEmpty)
            helper.fail("stack item not consumed")

        if (player.getItemInHand(hand.otherHand).isEmpty)
            helper.fail("stack item consumed from other hand")

        helper.succeedWhen {
            helper.assertBlockPresent(
                when (torchType) {
                    TorchType.VANILLA -> variant.standingBlock
                    TorchType.STICKY -> variant.wallBlock
                }
            )
        }
    }
}

class ThrownEntityOwner {
    @GameTest
    fun testPlayerIsOwner(helper: GameTestHelper) {
        helper.applyArena(EnclosedBox())
        val hand = InteractionHand.MAIN_HAND
        val player = helper.makePlayerAt(playerOrigin)
        player.setItemInHand(hand, ItemStack(TorchVariant.TORCH.itemOfType(TorchType.STICKY)))

        if (!TorchThrowing.throwTorch(player, hand))
            helper.fail("torch not thrown")

        val entity = helper.findClosestEntity(
            ModEntityTypes.THROWABLE_TORCH_ENTITY,
            playerOrigin.x,
            playerOrigin.y,
            playerOrigin.z,
            5.0
        )

        if (entity.owner != player)
            helper.fail("entity not owned by player")

        helper.succeed()
    }
}

class ThrowHonorsRotation {
    @GameTest(maxTicks = 100)
    fun north(helper: GameTestHelper) = test(helper, Cardinal.NORTH)

    @GameTest(maxTicks = 100)
    fun east(helper: GameTestHelper) = test(helper, Cardinal.EAST)

    @GameTest(maxTicks = 100)
    fun south(helper: GameTestHelper) = test(helper, Cardinal.SOUTH)

    @GameTest(maxTicks = 100)
    fun west(helper: GameTestHelper) = test(helper, Cardinal.WEST)

    private fun test(helper: GameTestHelper, facing: Cardinal) {
        val arena = EnclosedBox().also { helper.applyArena(it) }
        // FakePlayer provides connection required by forceSetRotation, makeMockServerPlayer does not
        val player = FakePlayer.get(helper.level)
        player.setGameMode(GameType.SURVIVAL)
        player.setPos(helper.absolutePos(playerOrigin).center)
        player.forceSetRotation(facing.yaw, false, 0f, false)

        val variant = TorchVariant.TORCH
        val hand = InteractionHand.MAIN_HAND
        player.setItemInHand(hand, ItemStack(variant.itemOfType(TorchType.STICKY)))

        if (!TorchThrowing.throwTorch(player, hand))
            helper.fail("torch not thrown")

        fun inExpectedSpot(pos: BlockPos): Boolean {
            return when (facing) {
                Cardinal.NORTH -> pos.x == playerOrigin.x && pos.z == arena.inside.zMin
                Cardinal.EAST -> pos.x == arena.inside.xMax && pos.z == playerOrigin.z
                Cardinal.SOUTH -> pos.x == playerOrigin.x && pos.z == arena.inside.zMax
                Cardinal.WEST -> pos.x == arena.inside.xMin && pos.z == playerOrigin.z
            }
        }

        helper.runAfterDelay(10) {
            val placed = helper.findBlockInVolume(arena.inside, variant.wallBlock)

            if (placed == null) {
                helper.fail("torch not placed")
            } else if (!inExpectedSpot(placed)) {
                helper.fail("torch not placed in expected position")
            } else {
                helper.assertBlockProperty(placed, BlockStateProperties.HORIZONTAL_FACING, facing.opposite)
            }

            helper.succeed()
        }
    }
}

class CreativeModeNonConsumption {
    @GameTest
    fun testItemCountUnchanged(helper: GameTestHelper) {
        helper.applyArena(EnclosedBox())
        val player = helper.makePlayerAt(playerOrigin, GameType.CREATIVE)
        val itemCount = 1
        val hand = InteractionHand.MAIN_HAND
        player.setItemInHand(hand, ItemStack(TorchVariant.TORCH.itemOfType(TorchType.STICKY), itemCount))

        if (!TorchThrowing.throwTorch(player, hand))
            helper.fail("torch not thrown")

        if (player.getItemInHand(hand).count != itemCount)
            helper.fail("stack item count changed")

        helper.succeed()
    }
}

class RejectNonTorchThrow {
    @GameTest(maxTicks = 100)
    fun testThrowStickMainHand(helper: GameTestHelper) = testThrowStick(helper, InteractionHand.MAIN_HAND)

    @GameTest(maxTicks = 100)
    fun testThrowStickOffHand(helper: GameTestHelper) = testThrowStick(helper, InteractionHand.OFF_HAND)

    private fun testThrowStick(helper: GameTestHelper, hand: InteractionHand) {
        val player = helper.makeMockServerPlayer(GameType.SURVIVAL)
        val itemCount = 10
        val createStack = { ItemStack(Items.STICK, itemCount) }
        player.setItemInHand(hand, createStack())
        player.setItemInHand(hand.otherHand, createStack())

        if (TorchThrowing.throwTorch(player, hand))
            helper.fail("stack item should not have been thrown")

        if (player.getItemInHand(hand).count != itemCount)
            helper.fail("stack item should not have been consumed")

        if (player.getItemInHand(hand.otherHand).count != itemCount)
            helper.fail("stack item should not have been consumed")

        helper.succeed()
    }

    @GameTest(maxTicks = 100)
    fun testThrowNothingMainHand(helper: GameTestHelper) = testThrowNothing(helper, InteractionHand.MAIN_HAND)

    @GameTest(maxTicks = 100)
    fun testThrowNothingOffHand(helper: GameTestHelper) = testThrowNothing(helper, InteractionHand.OFF_HAND)

    private fun testThrowNothing(helper: GameTestHelper, hand: InteractionHand) {
        val player = helper.makeMockServerPlayer(GameType.SURVIVAL)
        if (TorchThrowing.throwTorch(player, hand))
            helper.fail("nothing thrown")

        helper.succeed()
    }
}

class RejectThrowWithInvalidPlayerState {
    @GameTest
    fun testPlayerNotAlive(helper: GameTestHelper) =
        test(helper, helper.makeMockServerPlayer(GameType.SURVIVAL).also { it.health = 0.0f })

    @GameTest
    fun testPlayerIsSpectator(helper: GameTestHelper) = test(helper, helper.makeMockServerPlayer(GameType.SPECTATOR))

    private fun test(helper: GameTestHelper, player: Player) {
        val hand = InteractionHand.MAIN_HAND
        player.setItemInHand(hand, ItemStack(TorchVariant.TORCH.itemOfType(TorchType.STICKY)))

        if (TorchThrowing.throwTorch(player, hand)) {
            helper.fail("expected throw to fail")
        }

        helper.succeed()
    }
}

class CanThrow {
    @GameTest
    fun stickyIgnoresConfig(helper: GameTestHelper) {
        for (variant in TorchVariant.entries) {
            for (allowVanilla in listOf(true, false)) {
                val stack = ItemStack(ModItems.sticky(variant))

                if (!TorchThrowing.canThrow(stack, allowVanilla)) {
                    helper.fail("sticky ${variant.stickyName} rejected (allowVanilla=$allowVanilla)")
                }
            }
        }
        helper.succeed()
    }

    @GameTest
    fun vanillaHonorsConfig(helper: GameTestHelper) {
        for (variant in TorchVariant.entries) {
            val stack = ItemStack(variant.sourceItem)
            if (!TorchThrowing.canThrow(stack, true)) {
                helper.fail("vanilla ${variant.name} rejected (allowVanilla=true)")
            }
            if (TorchThrowing.canThrow(stack, false)) {
                helper.fail("vanilla ${variant.name} thrown (allowVanilla=false)")
            }
        }
        helper.succeed()
    }

    @GameTest
    fun nonTorchIsRejected(helper: GameTestHelper) {
        val stack = ItemStack(Items.STICK)
        for (allowVanilla in listOf(true, false)) {
            if (TorchThrowing.canThrow(stack, allowVanilla)) {
                helper.fail("non-torch item thrown (allowVanilla=$allowVanilla)")
            }
        }
        helper.succeed()
    }
}

class Recipes {
    @GameTest
    fun testVariantRecipes(helper: GameTestHelper) {
        for (variant in TorchVariant.entries) {
            val id = Torched.id(variant.stickyName)
            val key = ResourceKey.create(Registries.RECIPE, id)
            if (helper.level.server.recipeManager.byKey(key).isEmpty) {
                helper.fail("missing recipe: $id")
            }
        }
        helper.succeed()
    }

    @GameTest
    fun testCraftingRecipes(helper: GameTestHelper) {
        for (variant in TorchVariant.entries) {
            val input = CraftingInput.of(
                2, 2, listOf(
                    ItemStack(Items.SLIME_BALL), ItemStack(variant.sourceItem),
                    ItemStack.EMPTY, ItemStack.EMPTY,
                )
            )
            val match = helper.level.server.recipeManager.getRecipeFor(RecipeType.CRAFTING, input, helper.level)
            if (match.isEmpty) {
                helper.fail("crafting failed: ${variant.stickyName}")
            }
        }
        helper.succeed()
    }
}

//region Test Helpers

private val throwOrigin = BlockPos(3, 4, 3)
private val playerOrigin = BlockPos(3, 3, 3)

private fun BlockPos.withX(newX: Int) = BlockPos(newX, y, z)
private fun BlockPos.withY(newY: Int) = BlockPos(x, newY, z)
private fun BlockPos.withZ(newZ: Int) = BlockPos(x, y, newZ)
private val BlockPos.center: Vec3 get() = Vec3.atCenterOf(this)
private fun GameTestHelper.applyArena(arena: Arena) = applyVolumes(arena.volumes)
private fun GameTestHelper.applyVolumes(volumes: Sequence<BlockVolume>) = volumes.forEach { applyVolume(it) }
private fun GameTestHelper.applyVolume(volume: BlockVolume) = volume.positions.forEach { setBlock(it, volume.block) }
private fun GameTestHelper.findBlockInVolume(volume: BlockVolume, block: Block): BlockPos? =
    volume.positions.firstOrNull { getBlockState(it).`is`(block) }

private fun GameTestHelper.makePlayerAt(pos: BlockPos, mode: GameType = GameType.SURVIVAL) =
    makeMockServerPlayer(mode).also { player -> player.setPos(absolutePos(pos).center) }

private val InteractionHand.otherHand: InteractionHand
    get() = when (this) {
        InteractionHand.OFF_HAND -> InteractionHand.MAIN_HAND
        InteractionHand.MAIN_HAND -> InteractionHand.OFF_HAND
    }

private interface Arena {
    val volumes: Sequence<BlockVolume>
}

private open class EnclosedBox : Arena {
    val box = BlockVolume(Blocks.STONE, BlockPos(0, 0, 0), BlockPos(7, 7, 7))
    val inside = BlockVolume(Blocks.AIR, BlockPos(1, 1, 1), BlockPos(6, 6, 6))
    override val volumes = sequenceOf(box, inside)
}

private class EnclosedBoxWithWater : EnclosedBox() {
    val water = BlockVolume(Blocks.WATER, BlockPos(1, 1, 1), BlockPos(6, 3, 6))
    override val volumes = super.volumes + water
}

private class BlockVolume(val block: Block, p1: BlockPos, p2: BlockPos) {
    val xMin = min(p1.x, p2.x)
    val xMax = max(p1.x, p2.x)
    val yMin = min(p1.y, p2.y)
    val yMax = max(p1.y, p2.y)
    val zMin = min(p1.z, p2.z)
    val zMax = max(p1.z, p2.z)

    val positions = sequence {
        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                for (z in zMin..zMax) {
                    yield(BlockPos(x, y, z))
                }
            }
        }
    }
}

private fun TorchVariant.itemOfType(type: TorchType): Item = when (type) {
    TorchType.STICKY -> ModItems.sticky(this)
    TorchType.VANILLA -> this.sourceItem
}

private fun throwItemAtTarget(helper: GameTestHelper, item: Item, origin: Vec3, target: Vec3, pow: Float = 0.7f) =
    throwItemAtTarget(helper, item, origin, target, pow, owner = helper.makeMockServerPlayer(GameType.SURVIVAL))

private fun throwItemAtTarget(
    helper: GameTestHelper,
    item: Item,
    origin: Vec3,
    target: Vec3,
    pow: Float = 0.7f,
    owner: LivingEntity?
) =
    throwStackAtTarget(helper, ItemStack(item), origin, target, pow, owner)

private fun throwStackAtTarget(
    helper: GameTestHelper,
    stack: ItemStack,
    origin: Vec3,
    target: Vec3,
    pow: Float = 0.7f,
    owner: LivingEntity?
) =
    throwStackInDirection(helper, stack, origin, target.subtract(origin), pow, owner)

private fun throwStackInDirection(
    helper: GameTestHelper,
    stack: ItemStack,
    origin: Vec3,
    dir: Vec3,
    pow: Float,
    owner: LivingEntity?
) = throwStackInDirectionAbsolute(helper, stack, helper.absoluteVec(origin), dir, pow, owner)

private fun throwStackInDirectionAbsolute(
    helper: GameTestHelper,
    stack: ItemStack,
    origin: Vec3,
    dir: Vec3,
    pow: Float,
    owner: LivingEntity?
): ThrowableTorchEntity {
    val entity = if (owner != null) {
        ThrowableTorchEntity(helper.level, owner)
    } else {
        ThrowableTorchEntity(ModEntityTypes.THROWABLE_TORCH_ENTITY, helper.level)
    }

    entity.item = stack
    entity.setPos(origin.x, origin.y, origin.z)
    entity.shoot(dir.x, dir.y, dir.z, pow, 0.0f)
    helper.level.addFreshEntity(entity)

    return entity
}

enum class TorchType { VANILLA, STICKY }

enum class Cardinal(val direction: Direction) {
    NORTH(Direction.NORTH),
    EAST(Direction.EAST),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST);

    val yaw get() = direction.toYRot()
    val opposite get() = direction.opposite
}

//endregion
