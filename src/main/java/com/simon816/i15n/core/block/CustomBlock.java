package com.simon816.i15n.core.block;

import java.util.List;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.simon816.i15n.core.item.CustomItem;
import com.simon816.i15n.core.item.ItemBlockWrapper;
import com.simon816.i15n.core.item.ItemRegistry;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.world.CustomWorld;

public abstract class CustomBlock implements BlockNature {

    @Override
    public BlockData createData(CustomWorld world, Vector3i pos) {
        return null;
    }

    @Override
    public boolean onBlockPlacedByPlayer(CustomWorld world, Vector3i pos, Player player, ItemBlockWrapper item,
            ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean onBlockActivated(CustomWorld world, Vector3i pos, Player player, HandType currHand, Direction side,
            Vector3d clickPoint) {
        return false;
    }

    @Override
    public boolean onBlockHit(CustomWorld world, Vector3i pos, Player player, HandType currHand, Direction side,
            Vector3d clickPoint) {
        return true;
    }

    @Override
    public boolean onBlockBreak(CustomWorld world, Vector3i pos, Player player) {
        return true;
    }

    @Override
    public boolean onNeighborNotify(CustomWorld world, Vector3i pos, Vector3i neighbourPos, Direction side) {
        return true;
    }

    protected List<ItemStack> getDroppedItems() {
        List<ItemStack> items = Lists.newArrayList();
        CustomItem item = ItemRegistry.get(getKey());
        if (item != null) {
            items.add(item.createItemStack());
        }
        return items;
    }

    protected Entity createItemDrop(World world, Vector3i pos, ItemStack stack) {
        Entity entity = world.createEntity(EntityTypes.ITEM, pos.toDouble().add(0.5, 0.5, 0.5));
        entity.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
        return entity;
    }

    @Override
    public boolean onBlockHarvest(CustomWorld world, Vector3i pos, List<Entity> droppedEntities) {
        droppedEntities.clear();
        for (ItemStack item : getDroppedItems()) {
            Entity entity = createItemDrop(world.getWorld(), pos, item);
            if (entity != null) {
                droppedEntities.add(entity);
            }
        }
        return true;
    }

    @Override
    public void readDataAt(CustomWorld world, Vector3i pos, DataView data) {}

    @Override
    public void writeDataAt(CustomWorld world, Vector3i pos, DataView data) {}

}
