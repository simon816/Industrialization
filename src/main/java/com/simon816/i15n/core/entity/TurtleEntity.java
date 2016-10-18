package com.simon816.i15n.core.entity;

import java.util.Optional;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.simon816.i15n.core.ImplUtil;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.entity.turtle.TurtleTask;
import com.simon816.i15n.core.item.ItemRegistry;
import com.simon816.i15n.core.item.ItemWrench;
import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.i15n.core.world.WorldManager;

public class TurtleEntity extends CustomEntity implements EntityTracker {

    private final TrackerSerializer tracker = new TrackerSerializer();
    private SupportiveArmorStand<FallingBlock> block = new SupportiveArmorStand<>();
    private ArmorStand pickaxeStand;
    private boolean isSpawned;

    private final TurtleTask task = new TurtleTask(this);

    public TurtleEntity(World world) {
        super(world);
    }

    @Override
    public String getName() {
        return "Turtle";
    }

    @Override
    public void setPosition(Vector3d pos) {
        super.setPosition(pos);
        moveInternalEntities();
    }

    @Override
    public void onEntityActivated(Entity entity, Player player) {
        if (ItemWrench.isPlayerUsing(player)) {
            ImplUtil.showWritableBook(player, Lists.newArrayList(this.task.getCode()), newLines -> {
                if (!newLines.isEmpty()) {
                    Utils.runLater(() -> this.task.setCode(newLines.get(0)));
                }
            });
        }
    }

    @Override
    public void onEntityHit(Entity entity, Player player) {
        remove();
        Optional<String> dropRule = this.world.getGameRule("doEntityDrops");
        if (player.gameMode().get() != GameModes.CREATIVE && (!dropRule.isPresent()
                || Boolean.parseBoolean(dropRule.get()))) {
            dropAsItem();
        }
    }

    private void dropAsItem() {
        Optional<Entity> opEntity = this.world.createEntity(EntityTypes.ITEM, this.pos.add(0.5, 0.5, 0.5));
        if (!opEntity.isPresent()) {
            return;
        }
        ItemStack stack = ItemRegistry.get("turtle").createItemStack();
        opEntity.get().offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
        this.world.spawnEntity(opEntity.get(), WorldManager.SPAWN_CAUSE);
    }

    @Override
    public void onEntityRemoved(Entity entity) {}

    @Override
    public void setRotation(Vector3d rot) {
        super.setRotation(new Vector3d(0, rot.getY() % 360, 0));// Only y-axis supported
        moveInternalEntities();
    }

    @Override
    public void setPositionAndRotation(Vector3d pos, Vector3d rot) {
        this.pos = pos;
        this.rotation = new Vector3d(0, rot.getY() % 360, 0);
        moveInternalEntities();
    }

    private BlockState createBlockState() {
        return BlockTypes.DISPENSER.getDefaultState()
                .with(Keys.DIRECTION, Utils.rotationToDirection(this.rotation.getY())).get();
    }

    private void moveInternalEntities() {
        CustomWorld world = WorldManager.toCustomWorld(this.world);
        if (createInternalEntities(world)) {
            if (this.isSpawned) {
                // Re-attach passenger (some strange bug)
                this.block.spawn(WorldManager.SPAWN_CAUSE);
            }
            // positions
            this.block.getStand().setLocation(this.block.getStand().getLocation().setPosition(this.pos));
            this.pickaxeStand.setLocation(this.pickaxeStand.getLocation().setPosition(getPickaxePos()));

            // rotations
            BlockState blockState = this.block.getEntity().get(Keys.FALLING_BLOCK_STATE).get();
            Direction prevDir = blockState.get(Keys.DIRECTION).get();
            if (prevDir != Utils.rotationToDirection(this.rotation.getY())) {
                world.removeEntityFromTracker(this.block.getEntity(), this);
                this.block.setEntity(null, true);
                createInternalEntities(world);
                if (this.isSpawned) {
                    this.block.spawn(WorldManager.SPAWN_CAUSE);
                }
            }
            this.pickaxeStand.setRotation(getPickaxeRot());
        }
    }

    private boolean createInternalEntities(CustomWorld world) {
        if (!this.block.hasEntity()) {
            FallingBlock actualBlock =
                    (FallingBlock) this.world.createEntity(EntityTypes.FALLING_BLOCK, this.pos).orElse(null);
            if (!this.block.setEntity(actualBlock, true)) {
                return false;
            }
            world.addEntityToTracker(actualBlock, this);
            world.addEntityToTracker(this.block.getStand(), this);
            this.tracker.add("blockEntity", actualBlock);
            this.tracker.add("blockStand", this.block.getStand());
            actualBlock.offer(Keys.FALLING_BLOCK_STATE, createBlockState());
            actualBlock.offer(Keys.FALL_TIME, Integer.MIN_VALUE);
            actualBlock.offer(Keys.CAN_DROP_AS_ITEM, false);
        }
        if (this.pickaxeStand == null) {
            this.pickaxeStand =
                    (ArmorStand) this.world.createEntity(EntityTypes.ARMOR_STAND, getPickaxePos()).orElse(null);
            if (this.pickaxeStand == null) {
                return false;
            }
            ArmorStandData data = this.pickaxeStand.getOrCreate(ArmorStandData.class).get();
            data.set(data.gravity().set(false));
            data.set(data.basePlate().set(false));
            // data.set(data.marker().set(true));
            this.pickaxeStand.offer(data);
            ImplUtil.setInvisible(this.pickaxeStand, true);
            this.pickaxeStand.setRotation(getPickaxeRot());
            this.pickaxeStand.offer(Keys.RIGHT_ARM_ROTATION, new Vector3d(-45, 0, 0));
            this.pickaxeStand.setItemInHand(ItemStack.of(ItemTypes.DIAMOND_PICKAXE, 1));
            world.addEntityToTracker(this.pickaxeStand, this);
            this.tracker.add("toolStand", this.pickaxeStand);
        }
        return true;
    }

    private Vector3d getPickaxeRot() {
        Direction facing = Utils.rotationToDirection(this.rotation.getY());
        if (facing == Direction.NORTH) {
            return new Vector3d(0, 180, 0);
        }
        if (facing == Direction.EAST) {
            return new Vector3d(0, 270, 0);
        }
        if (facing == Direction.SOUTH) {
            return Vector3d.ZERO;
        }
        if (facing == Direction.WEST) {
            return new Vector3d(0, 90, 0);
        }
        return this.rotation;
    }

    private Vector3d getPickaxePos() {
        Direction facing = Utils.rotationToDirection(this.rotation.getY());
        if (facing == Direction.NORTH) {
            return this.pos.add(0.17, -0.7, 0.4);
        }
        if (facing == Direction.EAST) {
            return this.pos.add(-0.4, -0.7, 0.17);
        }
        if (facing == Direction.SOUTH) {
            return this.pos.add(-0.15, -0.7, -0.4);
        }
        if (facing == Direction.WEST) {
            return this.pos.add(0.4, -0.7, -0.15);
        }
        return this.pos;
    }

    @Override
    public boolean spawnInWorld(CustomWorld world) {
        if (!createInternalEntities(world)) {
            return false;
        }
        if (!this.block.spawn(WorldManager.SPAWN_CAUSE)) {
            return false;
        }
        if (!ImplUtil.realIsLoaded(this.pickaxeStand)
                && !this.world.spawnEntity(this.pickaxeStand, WorldManager.SPAWN_CAUSE)) {
            this.block.remove();
            return false;
        }
        // Ensure tracked (e.g. if set from readFrom)
        world.addEntityToTracker(this.block.getEntity(), this);
        world.addEntityToTracker(this.block.getStand(), this);
        world.addEntityToTracker(this.pickaxeStand, this);
        this.isSpawned = true;
        return true;
    }

    private boolean isValid() {
        return this.block.isValid() && ImplUtil.realIsLoaded(this.pickaxeStand);
    }

    @Override
    public void tick() {
        if (!isValid()) {
            remove();
            return;
        }
        this.task.tick();
        hackBlockFalling();
    }

    /**
     * Stops the fallingblock from falling. It 'falls' even when on an armor stand. If the Y
     * position is on a block, offset it slightly to stop the fallingblock from despawning.
     */
    private void hackBlockFalling() {
        FallingBlock fallingBlock = this.block.getEntity();
        Vector3d pos2 = fallingBlock.getLocation().getPosition();
        fallingBlock.offer(Keys.VELOCITY, Vector3d.ZERO);
        double y = this.pos.getY();
        if ((int) y == y) {
            y -= 0.05;
        }
        fallingBlock.setLocation(new Location<>(this.world, pos2.getX(), y, pos2.getZ()));
    }

    private void remove() {
        WorldManager.toCustomWorld(this.world).removeEntity(this);
    }

    @Override
    public void onRemoved() {
        if (this.block.getStand() != null) {
            WorldManager.toCustomWorld(this.world).removeEntityFromTracker(this.block.getStand(), this);
            this.tracker.remove("blockStand");
        }
        if (this.block.hasEntity()) {
            WorldManager.toCustomWorld(this.world).removeEntityFromTracker(this.block.getEntity(), this);
            this.tracker.remove("blockEntity");
        }
        this.block.remove();
        if (this.pickaxeStand != null) {
            this.pickaxeStand.remove();
            WorldManager.toCustomWorld(this.world).removeEntityFromTracker(this.pickaxeStand, this);
            this.tracker.remove("toolStand");
        }
        this.isSpawned = false;
    }

    @Override
    public void readFrom(DataView data) {
        super.readFrom(data);

        this.tracker.readFrom(this.world, data);
        this.pickaxeStand = (ArmorStand) this.tracker.get("toolStand");
        this.block.setStand((ArmorStand) this.tracker.get("blockStand"));
        FallingBlock falling = (FallingBlock) this.tracker.get("blockEntity");
        this.block.setEntity(falling, false);
        if (falling != null) {
            // Vanilla serializes this as a byte so we need to set it back to Integer.MIN_VALUE
            falling.offer(Keys.FALL_TIME, Integer.MIN_VALUE);
        }

        this.task.readFrom(data.getView(of("task")).get());
    }

    @Override
    public void writeTo(DataView data) {
        super.writeTo(data);
        this.tracker.writeTo(data);
        DataView taskData = data.createView(of("task"));
        this.task.writeTo(taskData);
    }

}
