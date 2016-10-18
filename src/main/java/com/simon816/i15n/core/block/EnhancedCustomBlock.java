package com.simon816.i15n.core.block;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.item.ItemBlockWrapper;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.world.CustomWorld;

/**
 * A custom block with extra world hooks.
 */
public abstract class EnhancedCustomBlock extends CustomBlock {

    public BlockSnapshot onBlockPlacedByPlayer(CustomWorld world, Vector3i pos, BlockSnapshot blockSnapshot,
            Player player, ItemBlockWrapper item, ItemStack itemStack) {
        return onBlockPlacedByPlayer(world, pos, player, item, itemStack) ? blockSnapshot : null;
    }

    public BlockSnapshot onBlockBreak(CustomWorld world, Vector3i pos, BlockSnapshot blockSnapshot, Player player) {
        return onBlockBreak(world, pos, player) ? blockSnapshot : null;
    }

    public BlockSnapshot onMovedByPiston(CustomWorld world, Vector3i pos, BlockSnapshot proposedBlock,
            BlockData prevData, Vector3i oldPos) {
        return proposedBlock;
    }

}
