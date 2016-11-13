package com.simon816.i15n.core.tile.pipe;

import java.util.List;
import java.util.UUID;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.ImplUtil;
import com.simon816.i15n.core.tile.PipeTileData;
import com.simon816.i15n.core.world.WorldManager;

public class ItemStackHolder implements PipeObject {

    private ItemStack stack;
    private Vector3d pos;
    private Direction from;
    private Direction dest;
    private float speed;
    private boolean reachedCenter;
    private UUID entityRef;

    private boolean finish;
    private ArmorStand itemStand;

    public ItemStackHolder() {}

    public ItemStackHolder(ItemStack stack, Direction fromSide, Direction destSide, Vector3i pipePos) {
        this.stack = stack;
        this.from = fromSide;
        this.dest = destSide;
        this.pos = pipePos.toDouble().add(calcOffset());
        this.speed = 0.05F;
    }

    @Override
    public Direction getDestSide() {
        return this.dest;
    }

    @Override
    public Direction getFromSide() {
        return this.from;
    }

    @Override
    public void setDestSide(Direction side) {
        if (side == this.dest) {
            return;
        }
        this.dest = side;
        this.finish = false;
        this.reachedCenter = false;
    }

    @Override
    public void setFromSide(Direction side) {
        if (side == this.from) {
            return;
        }
        this.from = side;
        this.finish = false;
        this.reachedCenter = false;
    }

    @Override
    public void onTransfer() {
        this.reachedCenter = false;
        this.finish = false;
    }

    @Override
    public void lostNeighbour(Direction newDest) {
        if (this.reachedCenter && this.dest != Direction.NONE) {
            setFromSide(this.dest); // return back to center
        }
        setDestSide(newDest);
    }

    @Override
    public void continueTravel(PipeTileData data) {
        if (this.itemStand == null || this.itemStand.isRemoved()) {
            spawnItem(data.getWorld().getWorld());
        }
        if (this.itemStand == null) {
            return;
        }
        if (!this.finish) {
            if (this.dest == Direction.NONE || !this.reachedCenter) {
                addPos(this.from.getOpposite().asOffset().mul(this.speed));
                updateProgress(data.getPosition(), this.from.getOpposite(), true);
                this.reachedCenter = this.finish;
                if (this.dest != Direction.NONE) {
                    // We where heading somewhere, resume
                    this.finish = false;
                }
            } else {
                addPos(this.dest.asOffset().mul(this.speed));
                updateProgress(data.getPosition(), this.dest, false);
            }
        }
    }

    private void addPos(Vector3d shift) {
        this.pos = this.pos.add(shift);
        this.itemStand.setLocation(this.itemStand.getLocation().setPosition(this.pos));
    }

    private void updateProgress(Vector3i pipePos, Direction dest, boolean toCenter) {
        Vector3d target = pipePos.toDouble().add(0.5, 0.3, 0.5);
        if (!toCenter) {
            target = target.add(dest.asOffset().mul(0.5));
        }

        double dist;
        if (dest == Direction.WEST) {// (-1, 0, 0)
            dist = this.pos.getX() - pipePos.getX();
        } else if (dest == Direction.EAST) {// (1, 0, 0)
            dist = (pipePos.getX() + 1) - this.pos.getX();
        } else if (dest == Direction.NORTH) {// (0, 0, -1
            dist = this.pos.getZ() - pipePos.getZ();
        } else if (dest == Direction.SOUTH) {// (0, 0, 1)
            dist = (pipePos.getZ() + 1) - this.pos.getZ();
        } else if (dest == Direction.UP) {// (0, 1, 0)
            dist = (pipePos.getY() + 0.8) - this.pos.getY();
        } else if (dest == Direction.DOWN) {// (0, -1, 0)
            dist = (this.pos.getY() + 0.3) - pipePos.getY();
        } else {
            return;
        }
        if (toCenter) {
            dist -= 0.5;
        }
        if (dist <= 0) { // On or past the target
            this.pos = target;
            this.itemStand.setLocation(this.itemStand.getLocation().setPosition(target));
            this.finish = true;
        } else {
            this.finish = false;
        }
    }

    @Override
    public boolean ready() {
        return this.finish;
    }

    @Override
    public ItemStack get() {
        if (this.finish) {
            return this.stack;
        }
        return null;
    }

    @Override
    public void drop(World world, Vector3i pipePos) {
        if (this.itemStand != null) {
            List<Entity> passengers = this.itemStand.getPassengers();
            for (Entity p : passengers) {
                Item item = (Item) p;
                item.setVehicle(null);
                ImplUtil.setPickupDelay(item, 10); // 10 is default
                ImplUtil.setDespawnTime(item, 6000); // 6000 is default
            }
            this.itemStand.remove();
        } else {
            Vector3d pos = pipePos.toDouble().add(0.5, 0.5, 0.5);
            Item item = createItem(world, pos, this.stack, false);
            if (item != null) {
                world.spawnEntity(item, WorldManager.SPAWN_CAUSE);
            }
        }
    }

    @Override
    public void destroy() {
        if (this.itemStand != null) {
            List<Entity> passengers = this.itemStand.getPassengers();
            for (Entity p : passengers) {
                p.remove();
            }
            this.itemStand.clearPassengers();
            this.itemStand.remove();
            this.itemStand = null;
        }
        this.finish = false;
        this.reachedCenter = false;
    }

    private Vector3d calcOffset() {
        Vector3d offset = Vector3d.ZERO;
        if (this.from == Direction.NORTH) {
            offset = new Vector3d(0.5, 0.3, 0);
        } else if (this.from == Direction.SOUTH) {
            offset = new Vector3d(0.5, 0.3, 1);
        } else if (this.from == Direction.EAST) {
            offset = new Vector3d(1, 0.3, 0.5);
        } else if (this.from == Direction.WEST) {
            offset = new Vector3d(0, 0.3, 0.5);
        } else if (this.from == Direction.UP) {
            offset = new Vector3d(0.5, 1, 0.5);
        } else if (this.from == Direction.DOWN) {
            offset = new Vector3d(0.5, 0, 0.5);
        }
        return offset;
    }

    private void spawnItem(World world) {
        if (this.entityRef != null) {
            this.itemStand = (ArmorStand) world.getEntity(this.entityRef).orElse(null);
            this.entityRef = null;
            if (this.itemStand != null) {
                return;
            }
        }
        this.itemStand = createStand(world, this.pos);
        Item itemEntity = createItem(world, this.pos, this.stack, true);
        if (!this.itemStand.addPassenger(itemEntity).isSuccessful()) {
            this.itemStand = null;
            return; // Attach passenger failed
        }
        if (!world.spawnEntity(this.itemStand, WorldManager.SPAWN_CAUSE)) {
            this.itemStand = null;
            return; // Spawn stand failed
        }
        if (!world.spawnEntity(itemEntity, WorldManager.SPAWN_CAUSE)) {
            this.itemStand = null;
            return; // Spawn item failed
        }
    }

    private static ArmorStand createStand(World world, Vector3d pos) {
        ArmorStand stand = (ArmorStand) world.createEntity(EntityTypes.ARMOR_STAND, pos);
        ArmorStandData data = stand.getOrCreate(ArmorStandData.class).get();
        data.set(data.small().set(true));
        data.set(data.basePlate().set(false));
        data.set(data.marker().set(true));
        stand.offer(data);
        stand.offer(Keys.HAS_GRAVITY, false);
        stand.tryOffer(Keys.INVISIBLE, true);
        return stand;
    }

    private static Item createItem(World world, Vector3d pos, ItemStack stack, boolean noPickup) {
        Item itemEntity = (Item) world.createEntity(EntityTypes.ITEM, pos);
        itemEntity.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
        if (noPickup) {
            ImplUtil.setInfinitePickupDelay(itemEntity);
            ImplUtil.setInfiniteDespawnTime(itemEntity);
        }
        return itemEntity;
    }

    @Override
    public void writeTo(DataView data) {
        data.set(of("item"), this.stack);
        data.set(of("position"), this.pos);
        data.set(of("from"), this.from.name());
        data.set(of("destination"), this.dest.name());
        data.set(of("speed"), this.speed);
        data.set(of("reachedCenter"), this.reachedCenter);
        if (this.itemStand != null && !this.itemStand.isRemoved()) {
            data.set(of("entityRef"), this.itemStand.getUniqueId());
        }
    }

    @Override
    public void readFrom(DataView data) {
        this.stack = data.getSerializable(of("item"), ItemStack.class).get();
        this.pos = data.getObject(of("position"), Vector3d.class).get();
        this.from = Direction.valueOf(data.getString(of("from")).get());
        this.dest = Direction.valueOf(data.getString(of("destination")).get());
        this.speed = data.getFloat(of("speed")).orElse(0.05F);
        this.reachedCenter = data.getBoolean(of("reachedCenter")).get();
        this.entityRef = data.getObject(of("entityRef"), UUID.class).orElse(null);
    }

}
