package com.simon816.i15n.core.world;

import java.util.Optional;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.block.BlockNature;
import com.simon816.i15n.core.entity.CustomEntity;
import com.simon816.i15n.core.entity.EntityTracker;
import com.simon816.i15n.core.tile.BlockData;

/**
 * A world aware of custom objects.
 */
public interface CustomWorld {

    BlockNature getBlock(Vector3i pos);

    default BlockState getRealBlock(Vector3i pos) {
        return getWorld().getBlock(pos);
    }

    default Optional<TileEntity> getRealBlockData(Vector3i pos) {
        return getWorld().getTileEntity(pos);
    }

    BlockData getBlockData(Vector3i pos);

    World getWorld();

    void setBlockWithData(Vector3i pos, BlockNature block, BlockData data);

    void removeBlock(Vector3i pos);

    void notifyAroundPoint(Vector3i pos);

    boolean spawnEntity(CustomEntity entity);

    void removeEntity(CustomEntity entity);

    EntityTracker getEntityTracker(Entity entity);

    void addEntityToTracker(Entity entity, EntityTracker tracker);

    void removeEntityFromTracker(Entity entity, EntityTracker tracker);

    AdditionalBlockInfo getBlockInfo(Vector3i pos);

    void setBlockInfo(Vector3i pos, AdditionalBlockInfo info);

}
