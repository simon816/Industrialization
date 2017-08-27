package com.simon816.i15n.core.block;

import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.simon816.i15n.core.ImplUtil;
import com.simon816.i15n.core.Industrialization;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.entity.MultiEntityStructure;
import com.simon816.i15n.core.entity.MultiEntityStructure.StructureInstance;
import com.simon816.i15n.core.inv.InventoryAdapter;
import com.simon816.i15n.core.inv.InventoryProvider;
import com.simon816.i15n.core.item.ItemBlockWrapper;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.tile.PipeTileData;
import com.simon816.i15n.core.tile.pipe.IPipeConnectable;
import com.simon816.i15n.core.tile.pipe.InventoryPipeConnection;
import com.simon816.i15n.core.world.AdditionalBlockInfo;
import com.simon816.i15n.core.world.CustomWorld;

public class PipeBlock extends EnhancedCustomBlock {

    private final MultiEntityStructure entityStruct;
    private final ItemStack pane;

    private void setupArmorStand(ArmorStand stand, Vector3d rotation) {
        ArmorStandData data = stand.getOrCreate(ArmorStandData.class).get();
        data.set(data.basePlate().set(false));
        data.set(data.marker().set(true));
        stand.offer(data);
        stand.offer(Keys.HAS_GRAVITY, false);
        if (rotation != null) {
            stand.offer(Keys.HEAD_ROTATION, rotation);
        }
        stand.setRotation(Vector3d.ZERO);
        stand.setHelmet(this.pane);
        // EntityArmorStand entStand = (EntityArmorStand) stand;
        // entStand.preventEntitySpawning = false;
        // entStand.width = 0.5F;
        // entStand.height = 1.1F;
        // entStand.noClip = true;
        // AxisAlignedBB bb = entStand.getEntityBoundingBox();
        // entStand.setEntityBoundingBox(new AxisAlignedBB(bb.minX, bb.minY, bb.minZ,
        // bb.minX + 0.5, bb.minY + 0.5, bb.minZ + 0.5));

        stand.tryOffer(Keys.INVISIBLE, true);
    }

    public PipeBlock() {
        this.pane = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
        Vector3d rotX = new Vector3d(90, 0, 0);
        Vector3d rotY = new Vector3d(0, 90, 0);
        this.entityStruct = new MultiEntityStructure.Builder()
                .define(Direction.DOWN.name(), EntityTypes.ARMOR_STAND,
                        armorStandOffset(true, Axis.Z, new Vector3f(0.5, 0.2, 0.5)),
                        ArmorStand.class, stand -> setupArmorStand(stand, rotX))
                .define(Direction.UP.name(), EntityTypes.ARMOR_STAND,
                        armorStandOffset(true, Axis.Z, new Vector3f(0.5, 0.7375, 0.5)),
                        ArmorStand.class, stand -> setupArmorStand(stand, rotX))
                .define(Direction.NORTH.name(), EntityTypes.ARMOR_STAND,
                        armorStandOffset(false, Axis.Z, new Vector3f(0.5, 0.2, 0.2)),
                        ArmorStand.class, stand -> setupArmorStand(stand, null))
                .define(Direction.SOUTH.name(), EntityTypes.ARMOR_STAND,
                        armorStandOffset(false, Axis.Z, new Vector3f(0.5, 0.2, 0.8)),
                        ArmorStand.class, stand -> setupArmorStand(stand, null))
                .define(Direction.EAST.name(), EntityTypes.ARMOR_STAND,
                        armorStandOffset(false, Axis.X, new Vector3f(0.8, 0.2, 0.5)),
                        ArmorStand.class, stand -> setupArmorStand(stand, rotY))
                .define(Direction.WEST.name(), EntityTypes.ARMOR_STAND,
                        armorStandOffset(false, Axis.X, new Vector3f(0.2, 0.2, 0.5)),
                        ArmorStand.class, stand -> setupArmorStand(stand, rotY))
                .build();
    }

    private static Vector3f armorStandOffset(boolean isHeadFlat, Axis facingAxis, Vector3f off) {
        return new Vector3f(isHeadFlat && facingAxis == Axis.X ? -0.75 : facingAxis == Axis.X ? -0.25 : 0,
                isHeadFlat ? -1.69 : -1.89,
                isHeadFlat && facingAxis == Axis.Z ? -0.75 : facingAxis == Axis.Z ? 0.295 : 0).add(off);
    }

    @Override
    public String getName() {
        return "Pipe";
    }

    @Override
    public BlockData createData(CustomWorld world, Vector3i pos) {
        return new PipeTileData(world, pos);
    }

    @Override
    public BlockSnapshot onBlockPlacedByPlayer(CustomWorld world, Vector3i pos, BlockSnapshot blockSnapshot,
            Player player, ItemBlockWrapper item, ItemStack itemStack) {
        this.entityStruct.initialize(world, pos);
        return blockSnapshot.withState(BlockTypes.BARRIER.getDefaultState());
    }

    @Override
    public boolean onBlockActivated(CustomWorld world, Vector3i pos, Player player, HandType currHand, Direction side,
            Vector3d clickPoint) {
        return true;
    }

    @Override
    public boolean onBlockHit(CustomWorld world, Vector3i pos, Player player, HandType currHand, Direction side,
            Vector3d clickPoint) {
        Cause breakCause = Cause.builder()
                .named("plugin", Industrialization.toContainer())
                .named(NamedCause.SOURCE, player)
                .build();
        world.getWorld().setBlockType(pos, BlockTypes.AIR, BlockChangeFlag.ALL, breakCause);
        // Sponge broke and doesn't fire the event (SpongeCommon#998)
        BlockSnapshot from = world.getWorld().createSnapshot(pos);
        BlockSnapshot to = from.withState(BlockTypes.AIR.getDefaultState());
        List<Transaction<BlockSnapshot>> tr = Lists.newArrayList(new Transaction<>(from, to));
        Sponge.getEventManager().post(SpongeEventFactory.createChangeBlockEventBreak(breakCause, tr));
        return false;
    }

    @Override
    public boolean onBlockBreak(CustomWorld world, Vector3i pos, Player player) {
        getStructure(world, pos).remove();
        ((PipeTileData) world.getBlockData(pos)).onBreak();
        if (player == null || player.gameMode().get() != GameModes.CREATIVE) {
            fireDropItemEvent(world.getWorld(), pos);
        }
        ImplUtil.playBlockBreakSound(world.getWorld(), pos, BlockTypes.GLASS.getDefaultState(), player);
        return true;
    }

    private void fireDropItemEvent(World world, Vector3i pos) {
        List<ItemStack> stacks = getDroppedItems();
        if (stacks.isEmpty()) {
            return;
        }
        List<Entity> entities = Lists.newArrayList();
        for (ItemStack stack : stacks) {
            Entity drop = createItemDrop(world, pos, stack);
            entities.add(drop);
        }
        Cause cause = Cause.source(BlockSpawnCause.builder()
                .type(SpawnTypes.DROPPED_ITEM)
                .block(world.createSnapshot(pos))
                .build())
                .build();
        DropItemEvent.Destruct harvestEvent = SpongeEventFactory.createDropItemEventDestruct(cause, entities);
        if (Sponge.getEventManager().post(harvestEvent)) {
            return;
        }
        for (Entity entity : harvestEvent.getEntities()) {
            world.spawnEntity(entity, cause);
        }
    }

    @Override
    public boolean onNeighborNotify(CustomWorld world, Vector3i pos, Vector3i neighbourPos, Direction side) {
        IPipeConnectable connection = getConnectionAt(world, neighbourPos);
        if (connection != null) {
            join(world, pos, side, neighbourPos);
        } else {
            disconnect(world, pos, side);
        }
        BlockData pipeTile = world.getBlockData(pos);
        ((PipeTileData) pipeTile).setNeighbor(side, connection);
        return true;

    }


    private static StructureInstance getStructure(CustomWorld world, Vector3i pos) {
        AdditionalBlockInfo data = world.getBlockInfo(pos);
        if (data instanceof StructureInstance) {
            return (StructureInstance) data;
        }
        return null;
    }

    private static IPipeConnectable getConnectionAt(CustomWorld world, Vector3i pos) {
        BlockData data = world.getBlockData(pos);
        if (data instanceof IPipeConnectable) {
            return (IPipeConnectable) data;
        }
        if (data instanceof InventoryProvider) {
            return InventoryPipeConnection.from(((InventoryProvider) data).getInventory());
        }
        InventoryAdapter inv = Utils.getInventory(world.getWorld(), pos);
        return InventoryPipeConnection.from(inv);
    }

    private void disconnect(CustomWorld world, Vector3i pos, Direction side) {
        StructureInstance struct = getStructure(world, pos);
        if (struct == null) {
            return;
        }
        struct.remove(shareId(side, 1));
        struct.remove(shareId(side, 2));
        struct.remove(shareId(side, 3));
        struct.remove(shareId(side, 4));
        struct.recreate(this.entityStruct, side.name());
    }

    private void join(CustomWorld world, Vector3i pos, Direction side, Vector3i pos2) {
        StructureInstance struct1 = getStructure(world, pos);
        if (struct1 == null) {
            return;
        }
        StructureInstance struct2 = getStructure(world, pos2);
        if (struct2 == null) {
            connect(struct1, null, side);
        } else if (side == Direction.DOWN || side == Direction.NORTH || side == Direction.WEST) {
            connect(struct1, struct2, side);
        } else {
            connect(struct2, struct1, side.getOpposite());
        }
        struct1.remove(side.name());
    }

    private void connect(StructureInstance struct1, StructureInstance struct2, Direction s1Side) {
        struct1.remove(s1Side.name());
        if (struct2 != null) {
            struct2.remove(s1Side.getOpposite().name());
        }
        if (struct1.get(shareId(s1Side, 1)) != null) {
            return; // Already connected
        }
        double rotY = 0;
        if (s1Side == Direction.NORTH || s1Side == Direction.SOUTH) {
            rotY = 90;
        }
        Vector3d rot = new Vector3d(0, rotY, 0);
        Vector3f offset;
        if (s1Side == Direction.WEST) {
            offset = new Vector3f(0, 0.2, 0.2);
        } else if (s1Side == Direction.EAST) {
            offset = new Vector3f(1, 0.2, 0.2);
        } else if (s1Side == Direction.NORTH) {
            offset = new Vector3f(0.2, 0.2, 0);
        } else if (s1Side == Direction.SOUTH) {
            offset = new Vector3f(0.2, 0.2, 1);
        } else if (s1Side == Direction.DOWN) {
            offset = new Vector3f(0.5, -0.2, 0.2);
        } else {
            offset = new Vector3f(0.75, 0.8, 0.5);
        }
        Vector3f offsetPos =
                armorStandOffset(false, s1Side == Direction.WEST || s1Side == Direction.EAST || s1Side == Direction.DOWN
                        ? Axis.Z : Axis.X, offset);

        struct1.lateBind(shareId(s1Side, 1), EntityTypes.ARMOR_STAND, offsetPos, ArmorStand.class,
                stand -> setupArmorStand(stand, rot));

        if (s1Side == Direction.NORTH || s1Side == Direction.SOUTH) {
            offsetPos = offsetPos.add(0.6, 0, 0);
        } else {
            offsetPos = offsetPos.add(0, 0, 0.6);
        }

        struct1.lateBind(shareId(s1Side, 2), EntityTypes.ARMOR_STAND, offsetPos, ArmorStand.class,
                stand -> setupArmorStand(stand, rot));

        Vector3d rot2 = new Vector3d(0, rotY, 0);
        if (s1Side != Direction.UP && s1Side != Direction.DOWN) {
            if (s1Side == Direction.WEST || s1Side == Direction.EAST) {
                if (s1Side == Direction.WEST) {
                    offset = new Vector3f(0, 0.2, 0.5);
                } else {
                    offset = new Vector3f(1, 0.2, 0.5);
                }
            } else {
                if (s1Side == Direction.NORTH) {
                    offset = new Vector3f(1.3, 0.2, 0.75);
                } else {
                    offset = new Vector3f(1.3, 0.2, 1.75);
                }
            }
            offsetPos = armorStandOffset(true, Axis.Z, offset);
            rot2 = rot2.add(90, 0, 0);
        } else {
            rot2 = rot2.add(0, 90, 0);
            offsetPos = offsetPos.add(0.05, 0, -0.59);
        }
        Vector3d finalRot2 = rot2;

        struct1.lateBind(shareId(s1Side, 3), EntityTypes.ARMOR_STAND, offsetPos, ArmorStand.class,
                stand -> setupArmorStand(stand, finalRot2));

        if (s1Side != Direction.UP && s1Side != Direction.DOWN) {
            offsetPos = offsetPos.add(0, 0.5375, 0);
        } else {
            offsetPos = offsetPos.add(-0.6, 0, 0);
        }

        struct1.lateBind(shareId(s1Side, 4), EntityTypes.ARMOR_STAND, offsetPos, ArmorStand.class,
                stand -> setupArmorStand(stand, finalRot2));

    }

    private static String shareId(Direction side, int i) {
        return "shared_" + side + "_" + i;
    }

    @Override
    public void readDataAt(CustomWorld world, Vector3i pos, DataView data) {
        super.readDataAt(world, pos, data);
        this.entityStruct.initFromData(world, pos, data.getView(DataQuery.of("struct")).get());
    }

    @Override
    public void writeDataAt(CustomWorld world, Vector3i pos, DataView data) {
        super.writeDataAt(world, pos, data);
        StructureInstance struct = getStructure(world, pos);
        if (struct == null) {
            return;
        }
        struct.writeTo(data.createView(DataQuery.of("struct")));
    }

}
