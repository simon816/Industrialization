package com.simon816.i15n.automation.pipes;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.simon816.i15n.core.ITickable;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.world.CustomWorld;

public class PipeTileData extends BlockData implements ITickable, IPipeConnectable {

    private final List<PipeObject> items = Lists.newArrayList();
    private final EnumMap<Direction, IPipeConnectable> connections = new EnumMap<>(Direction.class);

    public PipeTileData(CustomWorld world, Vector3i pos) {
        super(world, pos);
    }

    public void addItem(ItemStack item, Direction fromSide) {
        this.items.add(new ItemStackHolder(item, fromSide, findDestination(fromSide), getPosition()));
    }

    private Direction findDestination(Direction exclude) {
        if (this.connections.isEmpty()) {
            return Direction.NONE;
        }
        Iterator<Direction> itr = this.connections.keySet().iterator();
        Direction known = Direction.NONE;
        Random rand = new Random();
        while (itr.hasNext()) {
            Direction next = itr.next();
            if (next != exclude) {
                known = next;
                if (rand.nextBoolean()) {
                    return known;
                }
            }
        }
        return known;
    }

    @Override
    public ItemStack pull(Direction opposite) {
        // For now, you can't pull from pipes. Pipes will only push
        return null;
    }

    @Override
    public boolean push(ItemStack itemStack, Direction side) {
        addItem(itemStack, side);
        return true;
    }

    private boolean firstTick = true;

    @Override
    public void tick() {
        if (this.firstTick) {
            checkNeighbors();
            this.firstTick = false;
        }
        // Pull items from neighbours
        if (Utils.ticksPassed(10)) { // rate limit
            for (Entry<Direction, IPipeConnectable> e : this.connections.entrySet()) {
                Direction side = e.getKey();
                IPipeConnectable con = e.getValue();
                ItemStack stack = con.pull(side.getOpposite());
                if (stack != null) {
                    addItem(stack, side);
                }
            }
        }
        // Tick items
        for (Iterator<PipeObject> iterator = this.items.iterator(); iterator.hasNext();) {
            PipeObject obj = iterator.next();
            Direction dest = obj.getDestSide();
            if (!this.connections.containsKey(dest)) {
                obj.lostNeighbour(findDestination(dest));
            }
            obj.continueTravel(this);
            IPipeConnectable destCon = this.connections.get(obj.getDestSide());
            if (destCon != null && obj.ready()) {
                if (destCon instanceof PipeTileData) {
                    ((PipeTileData) destCon).continueItem(obj, obj.getDestSide().getOpposite());
                } else {
                    if (!destCon.push(obj.get(), obj.getDestSide().getOpposite())) {
                        obj.drop(this.world.getWorld(), getPosition());
                    }
                    obj.destroy();
                }
                iterator.remove();
            }
        }
    }

    private void checkNeighbors() {
        for (Direction side : Direction.values()) {
            if (!side.isCardinal() && !side.isUpright()) {
                continue;
            }
            getBlock().onNeighborNotify(this.world, getPosition(), getPosition().add(side.asBlockOffset()), side);
        }
    }

    private void continueItem(PipeObject obj, Direction newFace) {
        obj.setFromSide(newFace);
        Direction d = findDestination(newFace);
        obj.setDestSide(d == Direction.NONE ? newFace : d);
        obj.onTransfer();
        this.items.add(obj);
    }

    public void setNeighbor(Direction side, IPipeConnectable connection) {
        if (connection == null) {
            this.connections.remove(side);
        } else {
            this.connections.put(side, connection);
        }
    }

    public void onBreak() {
        for (Iterator<PipeObject> iterator = this.items.iterator(); iterator.hasNext();) {
            PipeObject obj = iterator.next();
            obj.drop(this.world.getWorld(), this.getPosition());
            obj.destroy();
            iterator.remove();
        }
        this.connections.clear();
    }

    @Override
    public void writeTo(DataView data) {
        super.writeTo(data);
        List<DataView> itemDataList = Lists.newArrayList();
        for (PipeObject obj : this.items) {
            itemDataList.add(obj.toContainer());
        }
        data.set(of("items"), itemDataList);
    }

    @Override
    public void readFrom(DataView data) {
        super.readFrom(data);
        List<DataView> itemDataList = data.getViewList(of("items")).get();
        for (DataView itemData : itemDataList) {
            ItemStackHolder obj = new ItemStackHolder();
            obj.readFrom(itemData);
            this.items.add(obj);
        }
    }

}
