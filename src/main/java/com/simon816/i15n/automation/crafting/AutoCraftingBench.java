package com.simon816.i15n.automation.crafting;

import java.util.Optional;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.block.CustomBlock;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.world.CustomWorld;

public class AutoCraftingBench extends CustomBlock {

    @Override
    public String getName() {
        return "Auto Crafting Bench";
    }

    @Override
    public BlockData createData(CustomWorld world, Vector3i pos) {
        return new TileAutoCrafting(world, pos);
    }

    @Override
    public boolean onBlockBreak(CustomWorld world, Vector3i pos, Player player) {
        // TODO possible cause/context
        ((TileAutoCrafting) world.getBlockData(pos)).destroy();
        return true;
    }

    @Override
    public boolean onBlockActivated(CustomWorld world, Vector3i pos, Player player, HandType currHand, Direction side,
            Vector3d clickPoint) {
        TileAutoCrafting te = (TileAutoCrafting) world.getBlockData(pos);
        // TODO possible cause/context
        Optional<Container> optContainer = player.openInventory(te.getInventory().getAPIInventory());
        if (optContainer.isPresent()) {
            te.addContainer(optContainer.get());
        }
        return false;
    }

}
