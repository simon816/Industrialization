package com.simon816.i15n.core.tile;

import org.spongepowered.api.data.DataView;

import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.Serialized;
import com.simon816.i15n.core.block.BlockNature;
import com.simon816.i15n.core.world.CustomWorld;

/**
 * Associated data for a particular blocknature at a particular location.
 * 
 */
public class BlockData implements Serialized {
    protected final CustomWorld world;
    private Vector3i pos;

    public BlockData(CustomWorld world, Vector3i pos) {
        this.world = world;
        this.pos = pos;
    }

    public Vector3i getPosition() {
        return this.pos;
    }

    public CustomWorld getWorld() {
        return this.world;
    }

    public void setPos(Vector3i newPos) {
        this.pos = newPos;
    }

    public BlockNature getBlock() {
        return this.world.getBlock(this.pos);
    }

    @Override
    public void readFrom(DataView data) {}

    @Override
    public void writeTo(DataView data) {}
}
