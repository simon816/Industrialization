package com.simon816.i15n.core.world;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.simon816.i15n.core.ITickable;
import com.simon816.i15n.core.Industrialization;
import com.simon816.i15n.core.Serialized;
import com.simon816.i15n.core.TickHelper;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.block.BlockNature;
import com.simon816.i15n.core.block.BlockRegistry;
import com.simon816.i15n.core.block.EnhancedCustomBlock;
import com.simon816.i15n.core.data.DataList;
import com.simon816.i15n.core.entity.CustomEntity;
import com.simon816.i15n.core.entity.EntityRegistry;
import com.simon816.i15n.core.entity.EntityTracker;
import com.simon816.i15n.core.tile.BlockData;

public class WorldManager {

    public static final Cause SPAWN_CAUSE = Cause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build())
            .named("Plugin", Industrialization.toContainer()).build();

    private static final Map<UUID, Tracker> trackers = Maps.newHashMap();

    public static CustomWorld toCustomWorld(World world) {
        Tracker tracker = trackers.get(world.getUniqueId());
        if (tracker == null) {
            trackers.put(world.getUniqueId(), tracker = new Tracker(world));
        }
        return tracker;
    }

    public static void loadWorld(World world, DataView data) {
        unload(world);
        Tracker newTracker = new Tracker(world);
        // Tracker added to Map before reading so calls to toCustomWorld while loading will
        // reference correct object
        // TODO ensure no use of toCustomWorld during loading
        trackers.put(world.getUniqueId(), newTracker);
        newTracker.readFrom(data);
    }

    public static boolean saveWorld(World world, DataView data) {
        Tracker tracker = trackers.get(world.getUniqueId());
        if (tracker != null) {
            tracker.writeTo(data);
            return true;
        }
        return false;
    }

    public static void unload(World world) {
        Tracker tracker = trackers.remove(world.getUniqueId());
        if (tracker != null) {
            tracker.destroy();
        }
    }

    private static class Tracker implements CustomWorld, Serialized {

        private final World world;
        final PistonMovementTracker pistonTracker;

        private final Map<Vector3i, BlockNature> trackedBlocks = Maps.newHashMap();
        private final Map<Vector3i, AdditionalBlockInfo> trackedBlockInfo = Maps.newHashMap();
        private final Map<Vector3i, BlockData> trackedBlockData = Maps.newHashMap();
        private final Map<UUID, EntityTracker> trackedEntities = Maps.newHashMap();
        private final Set<CustomEntity> trackedCustomEntities = Sets.newHashSet();


        public Tracker(World world) {
            this.world = world;
            this.pistonTracker = new PistonMovementTracker();
        }

        void destroy() {
            this.trackedBlocks.clear();
            this.trackedBlockInfo.clear();
            this.trackedBlockData.clear();
            this.trackedEntities.clear();
            this.trackedCustomEntities.clear();
        }

        @Override
        public void writeTo(DataView data) {
            DataList<DataView> blockList = Utils.createListView();
            for (Entry<Vector3i, BlockNature> entry : this.trackedBlocks.entrySet()) {
                DataView blockDataView = blockList.next();
                Vector3i pos = entry.getKey();
                BlockNature block = entry.getValue();
                blockDataView.set(of("position"), Utils.s(pos));
                DataView blockView = blockDataView.createView(of("block"));
                blockView.set(of("id"), block.getId());
                block.writeDataAt(this, pos, blockView);
                BlockData blockData = this.trackedBlockData.get(pos);
                if (blockData != null) {
                    blockDataView.set(of("blockData"), blockData);
                }
            }
            data.set(of("blocks"), blockList.getList());
            DataList<DataView> entityList = Utils.createListView();
            for (CustomEntity customEntity : this.trackedCustomEntities) {
                customEntity.writeTo(entityList.next());
            }
            data.set(of("entities"), entityList.getList());
        }

        @Override
        public void readFrom(DataView data) {
            List<DataView> blockDataList = data.getViewList(of("blocks")).get();
            for (DataView blockDataView : blockDataList) {
                Vector3i pos = blockDataView.getObject(of("position"), Vector3i.class).get();
                DataView blockView = blockDataView.getView(of("block")).get();
                String blockId = blockView.getString(of("id")).get();
                BlockNature block = BlockRegistry.get(blockId);
                if (block == null) {
                    continue;
                }
                block.readDataAt(this, pos, blockView);
                BlockData te = block.createData(this, pos);
                if (te != null) {
                    Optional<DataView> blockData = blockDataView.getView(of("blockData"));
                    if (blockData.isPresent()) {
                        te.readFrom(blockData.get());
                    }
                }
                setBlockWithData(pos, block, te);
            }
            List<DataView> entityDataList = data.getViewList(of("entities")).get();
            for (DataView entityData : entityDataList) {
                String entityId = entityData.getString(of("id")).get();
                CustomEntity entity = EntityRegistry.construct(entityId, this);
                if (entity == null) {
                    continue;
                }
                entity.readFrom(entityData);
                spawnEntity(entity);
            }
        }

        @Override
        public BlockNature getBlock(Vector3i pos) {
            return this.trackedBlocks.get(pos);
        }

        @Override
        public BlockData getBlockData(Vector3i pos) {
            return this.trackedBlockData.get(pos);
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public void setBlockWithData(Vector3i pos, BlockNature block, BlockData data) {
            if (this.trackedBlocks.putIfAbsent(pos, block) != null) {
                throw new IllegalStateException("Block already tracked at " + pos);
            }
            if (data != null) {
                this.trackedBlockData.put(pos, data);
                if (data instanceof ITickable) {
                    TickHelper.startTicking((ITickable) data);
                }
            }
        }

        @Override
        public void notifyAroundPoint(Vector3i pos) {
            for (Direction direction : Direction.values()) {
                if (direction.isCardinal() || direction.isUpright()) {
                    Vector3i targetPos = pos.add(direction.asBlockOffset());
                    BlockNature block = getBlock(targetPos);
                    if (block != null) {
                        block.onNeighborNotify(this, targetPos, pos, direction.getOpposite());
                    }
                }
            }
        }

        @Override
        public void removeBlock(Vector3i pos) {
            this.trackedBlocks.remove(pos);
            this.trackedBlockInfo.remove(pos);
            BlockData data = this.trackedBlockData.remove(pos);
            if (data instanceof ITickable) {
                TickHelper.stopTicking((ITickable) data);
            }
        }

        @Override
        public AdditionalBlockInfo getBlockInfo(Vector3i pos) {
            return this.trackedBlockInfo.get(pos);
        }

        @Override
        public void setBlockInfo(Vector3i pos, AdditionalBlockInfo info) {
            this.trackedBlockInfo.put(pos, info);
        }

        @Override
        public boolean spawnEntity(CustomEntity entity) {
            if (!entity.spawnInWorld(this)) {
                return false;
            }
            this.trackedCustomEntities.add(entity);
            TickHelper.startTicking(entity);
            return true;
        }

        @Override
        public void removeEntity(CustomEntity entity) {
            if (this.trackedCustomEntities.remove(entity)) {
                entity.onRemoved();
                TickHelper.stopTicking(entity);
            }
        }

        @Override
        public EntityTracker getEntityTracker(Entity entity) {
            return this.trackedEntities.get(entity.getUniqueId());
        }

        @Override
        public void addEntityToTracker(Entity entity, EntityTracker tracker) {
            if (tracker.acceptEntity(entity)) {
                this.trackedEntities.put(entity.getUniqueId(), tracker);
            }
        }

        @Override
        public void removeEntityFromTracker(Entity entity, EntityTracker tracker) {
            if (this.trackedEntities.remove(entity.getUniqueId(), tracker)) {
                tracker.stopTracking(entity);
            }
        }
    }

    public static PistonMovementTracker getPistonTracker(World world) {
        return ((Tracker) toCustomWorld(world)).pistonTracker;
    }

    public static class PistonMovementTracker {

        private final Map<Vector3i, Tuple<Vector3i, ImmutablePair<EnhancedCustomBlock, BlockData>>> locations =
                Maps.newHashMap();

        public PistonMovementTracker() {}

        public void addTarget(BlockSnapshot pistonBlock, EnhancedCustomBlock block, BlockData blockData,
                Vector3i blockPos) {
            Direction pistonFacing = pistonBlock.getState().get(Keys.DIRECTION).get();
            boolean isRetracting;
            if (pistonBlock.getState().getType() == BlockTypes.PISTON_EXTENSION) {
                // Followed up from sticky piston - retraction has cause of the piston extension
                // See CustomBlockEventListeners#getPistonCause
                isRetracting = true;
            } else {
                isRetracting = pistonBlock.getState().get(Keys.EXTENDED).get();
            }
            Vector3i destPos;
            if (isRetracting) {
                destPos = blockPos.sub(pistonFacing.asBlockOffset());
            } else {
                destPos = blockPos.add(pistonFacing.asBlockOffset());
            }
            this.locations.put(destPos, new Tuple<>(blockPos, new ImmutablePair<>(block, blockData)));
        }

        public boolean isTracked(Vector3i pistonPos) {
            return this.locations.containsKey(pistonPos);
        }

        public Tuple<Vector3i, ImmutablePair<EnhancedCustomBlock, BlockData>> restore(Vector3i newPos) {
            return this.locations.remove(newPos);
        }
    }

}
