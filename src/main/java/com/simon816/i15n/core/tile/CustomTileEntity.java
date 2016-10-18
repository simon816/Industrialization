package com.simon816.i15n.core.tile;

import org.spongepowered.api.block.BlockState;

import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.world.CustomWorld;

public abstract class CustomTileEntity extends BlockData {

    public CustomTileEntity(CustomWorld world, Vector3i pos) {
        super(world, pos);
    }

    public BlockState getRealBlock() {
        return this.world.getRealBlock(getPosition());
    }

}
