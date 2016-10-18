package com.simon816.i15n.core.block;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.item.ItemBlockWrapper;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.world.CustomWorld;

/**
 * A block-like component. Could be a real world Block or a virtual emulated block.
 *
 */
public interface BlockNature extends CatalogType {

    @Override
    default String getId() {
        return BlockRegistry.blockToId(this);
    }

    BlockData createData(CustomWorld world, Vector3i pos);


    boolean onBlockPlacedByPlayer(CustomWorld world, Vector3i pos, Player player, ItemBlockWrapper item,
            ItemStack itemStack);


    boolean onNeighborNotify(CustomWorld world, Vector3i pos, Vector3i neighbourPos, Direction side);

    boolean onBlockBreak(CustomWorld world, Vector3i pos, Player player);

    boolean onBlockActivated(CustomWorld world, Vector3i pos, Player player, Direction side,
            @Nullable Vector3d clickPoint);

    boolean onBlockHit(CustomWorld world, Vector3i pos, Player player, Direction side,
            @Nullable Vector3d clickPoint);

    boolean onBlockHarvest(CustomWorld world, Vector3i pos, List<Entity> droppedEntities);

    void writeDataAt(CustomWorld world, Vector3i pos, DataView data);

    void readDataAt(CustomWorld world, Vector3i pos, DataView data);

}
