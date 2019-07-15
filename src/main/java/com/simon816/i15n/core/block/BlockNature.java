package com.simon816.i15n.core.block;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.compat.CatalogKey;
import com.simon816.i15n.core.item.ItemBlockWrapper;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.world.CustomWorld;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A block-like component. Could be a real world Block or a virtual emulated block.
 *
 */
public interface BlockNature extends CatalogType {

    //@Override TODO API 8: This is an override
    default CatalogKey getKey() {
        return BlockRegistry.blockToKey(this);
    }
    @Override
    default String getId() {
        return getKey().toString();
    }

    BlockData createData(CustomWorld world, Vector3i pos);


    boolean onBlockPlacedByPlayer(CustomWorld world, Vector3i pos, Player player, ItemBlockWrapper item,
            ItemStack itemStack);


    boolean onNeighborNotify(CustomWorld world, Vector3i pos, Vector3i neighbourPos, Direction side);

    boolean onBlockBreak(CustomWorld world, Vector3i pos, Player player);

    boolean onBlockActivated(CustomWorld world, Vector3i pos, Player player, HandType currHand, Direction side,
            @Nullable Vector3d clickPoint);

    boolean onBlockHit(CustomWorld world, Vector3i pos, Player player, HandType currHand, Direction side,
            @Nullable Vector3d clickPoint);

    boolean onBlockHarvest(CustomWorld world, Vector3i pos, List<Entity> droppedEntities);

    void writeDataAt(CustomWorld world, Vector3i pos, DataView data);

    void readDataAt(CustomWorld world, Vector3i pos, DataView data);

}
