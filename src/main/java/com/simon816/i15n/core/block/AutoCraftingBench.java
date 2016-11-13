package com.simon816.i15n.core.block;

import java.util.Optional;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.tile.TileAutoCrafting;
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
        Cause.Builder cause = Cause.source(this);
        if (player != null) {
            cause.notifier(player);
        }
        ((TileAutoCrafting) world.getBlockData(pos)).destroy(cause.build());
        return true;
    }

    @Override
    public boolean onBlockActivated(CustomWorld world, Vector3i pos, Player player, HandType currHand, Direction side,
            Vector3d clickPoint) {
        TileAutoCrafting te = (TileAutoCrafting) world.getBlockData(pos);
        Optional<Container> optContainer = player.openInventory(
                te.getInventory().getAPIInventory(), Cause.source(this).notifier(player).build());
        if (optContainer.isPresent()) {
            te.addContainer(optContainer.get());
        }
        return false;
    }

}
