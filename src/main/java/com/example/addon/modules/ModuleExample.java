package com.meteor.client.module.modules.movement

import com.meteor.client.event.SafeClientEvent
import com.meteor.client.event.events.ConnectionEvent
import com.meteor.client.event.listener.listener
import com.meteor.client.module.Category
import com.meteor.client.module.Module
import com.meteor.client.util.TickTimer
import com.meteor.client.util.math.Direction
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object HoleSnap : Module(
    name = "HoleSnap",
    description = "Snaps player to the closest hole while moving",
    category = Category.MOVEMENT
) {
    private val messageTimer = TickTimer(TimeUnit.SECONDS)

    override fun isActive(): Boolean {
        return isEnabled
    }

    init {
        listener<InputUpdateEvent>(6969) {
            if (it.movementInput == null) return@listener
            
            val closestHole = findClosestHole()
            if (closestHole != null) {
                moveToHole(closestHole)
            }
        }

        listener<ConnectionEvent.Disconnect> {
            disable()
        }
    }

    private fun moveToHole(hole: BlockPos) {
        mc.player?.let { player ->
            player.moveForward = 1.0f // Move towards the hole

            // Calculate direction towards the hole
            val holeX = hole.x
            val holeZ = hole.z
            val directionToHole = Direction.fromPosition(holeX, holeZ)

            player.moveStrafing = directionToHole // Adjust for strafing if needed
        }
    }

    private fun findClosestHole(): BlockPos? {
        val holes = mutableListOf<BlockPos>()

        for (x in -5..5) {
            for (z in -5..5) {
                val pos = BlockPos(mc.player.posX + x.toDouble(), mc.player.posY - 1.0, mc.player.posZ + z.toDouble())
                if (isHole(pos)) {
                    holes.add(pos)
                }
            }
        }

        return holes.minByOrNull { it.distanceSq(mc.player.position) }
    }

    private fun isHole(pos: BlockPos): Boolean {
        val blockState = mc.world.getBlockState(pos)
        return blockState.isAir && // Check if the block is air (empty)
               mc.world.getBlockState(pos.up()).isSolid // Check if the block above is solid
    }
}
