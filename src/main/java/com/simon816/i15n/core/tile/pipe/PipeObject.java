package com.simon816.i15n.core.tile.pipe;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.Serialized;
import com.simon816.i15n.core.tile.PipeTileData;

public interface PipeObject extends Serialized {

    Direction getDestSide();

    void setDestSide(Direction side);

    void continueTravel(PipeTileData data);

    void destroy();

    boolean ready();

    ItemStack get();

    Direction getFromSide();

    void setFromSide(Direction side);

    void onTransfer();

    void drop(World world, Vector3i pipePos);

    void lostNeighbour(Direction newDest);

}
