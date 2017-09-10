package com.simon816.i15n.core.block;

import org.spongepowered.api.entity.living.player.Player;

import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.tile.TileMonitorDriver;
import com.simon816.i15n.core.world.CustomWorld;

public class MonitorDriverBlock extends CustomBlock {

    @Override
    public BlockData createData(CustomWorld world, Vector3i pos) {
        return new TileMonitorDriver(world, pos);
    }

    @Override
    public String getName() {
        return "Monitor Driver";
    }

    @Override
    public boolean onBlockBreak(CustomWorld world, Vector3i pos, Player player) {
        ((TileMonitorDriver) world.getBlockData(pos)).stop();
        return true;
    }
}
