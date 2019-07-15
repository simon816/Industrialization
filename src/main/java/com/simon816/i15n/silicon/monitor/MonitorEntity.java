package com.simon816.i15n.silicon.monitor;

import java.util.Arrays;
import java.util.Map;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import com.simon816.i15n.core.ImplUtil;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.entity.CustomEntity;
import com.simon816.i15n.core.entity.EntityTracker;
import com.simon816.i15n.core.entity.TrackerSerializer;
import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.i15n.core.world.WorldManager;

import net.minecraft.world.storage.MapData;

public class MonitorEntity extends CustomEntity implements EntityTracker {

    private final TrackerSerializer tracker = new TrackerSerializer();

    private FrameBuffer buffer;

    private int numCols;
    private int numRows;

    public MonitorEntity(World world, int cols, int rows) {
        super(world);
        this.numCols = cols;
        this.numRows = rows;
        if (this.numCols != 0 && this.numRows != 0) {
            this.buffer = new ArrayFrameBuffer(cols, rows);
        }
    }

    @Override
    public void setRotation(Vector3d rot) {
        super.setRotation(new Vector3d(0, rot.getY() % 360, 0));// Only y-axis supported
    }

    @Override
    public String getName() {
        return "Computer Monitor";
    }

    /**
     * Returns null if initialised with a zero size (e.g before reading from stored data)
     *
     * @return
     */
    public FrameBuffer getBuffer() {
        return this.buffer;
    }

    @Override
    public void tick() {}

    @Override
    public boolean spawnInWorld(CustomWorld world) {
        if (this.numCols == 0 || this.numRows == 0) {
            // Invalid
            return false;
        }
        int[] mapIds = reserveIdSpace(world.getWorld());
        FrameBuffer oldBuffer = this.buffer;
        this.buffer = createInternalBuffer(world.getWorld());
        if (oldBuffer != null) {
            oldBuffer.copyTo(this.buffer);
        }
        getBuffer().clear(PixelBaseColor.BLACK.baseVal());

        int idptr = 0;
        ItemStack mapItem = ItemStack.of(ItemTypes.FILLED_MAP, 1);
        Vector3i basePos = this.pos.toInt();
        Direction facing = Utils.rotationToDirection(this.rotation.getY());

        for (int yshift = this.numRows - 1; yshift >= 0; yshift--) {
            for (int colShift = 0; colShift < this.numCols; colShift++) {
                String tId = trackerId(yshift, colShift);
                ItemFrame itemFrame = (ItemFrame) this.tracker.get(tId);
                if (itemFrame == null) {
                    itemFrame = createFrame(world, mapItem, mapIds[idptr++], facing,
                            shiftPos(basePos, facing, colShift, yshift));
                    if (itemFrame != null) {
                        this.tracker.add(tId, itemFrame);
                    }
                }
                if (itemFrame == null) {
                    return false;
                }
                world.addEntityToTracker(itemFrame, this);
            }
        }
        return true;
    }

    private static ItemFrame createFrame(CustomWorld world, ItemStack mapItem, int mapId, Direction facing,
            Vector3i pos) {
        ItemFrame itemFrame = (ItemFrame) world.getWorld().createEntity(EntityTypes.ITEM_FRAME, pos);
        itemFrame.offer(itemFrame.direction().set(facing));
        ImplUtil.setItemDamage(mapItem, mapId);
        itemFrame.offer(Keys.REPRESENTED_ITEM, mapItem.createSnapshot());
        if (world.getWorld().spawnEntity(itemFrame)) {
            return itemFrame;
        }
        return null;
    }

    private static Vector3i shiftPos(Vector3i basePos, Direction facing, int colShift, int yShift) {
        int x;
        int z;
        if (facing == Direction.NORTH) {
            x = -colShift;
            z = 0;
        } else if (facing == Direction.WEST) {
            x = 0;
            z = colShift;
        } else if (facing == Direction.EAST) {
            x = 0;
            z = -colShift;
        } else {
            x = colShift;
            z = 0;
        }
        return basePos.add(x, yShift, z);
    }

    @Override
    public void writeTo(DataView data) {
        super.writeTo(data);
        data.set(of("width"), this.numCols);
        data.set(of("height"), this.numRows);
        data.set(of("idrange"), idMap.get(this));
        this.tracker.writeTo(data.createView(of("frames")));
    }

    @Override
    public void readFrom(DataView data) {
        super.readFrom(data);
        this.numCols = data.getInt(of("width")).orElse(0);
        this.numRows = data.getInt(of("height")).orElse(0);
        boolean valid = this.numCols != 0 && this.numRows != 0;
        if (valid) {
            this.buffer = new ArrayFrameBuffer(this.numCols, this.numRows);
        } else {
            this.buffer = null;
        }
        this.tracker.readFrom(this.world, data.getView(of("frames")).get());
        if (valid) {
            int[] range = (int[]) data.get(of("idrange")).get();
            if (range.length == this.numCols * this.numRows) {
                idMap.put(this, range);
            }
        } else {
            this.tracker.clear();
        }

    }

    private static String trackerId(int row, int col) {
        return new StringBuilder("frame_").append(row).append('_').append(col).toString();
    }

    @Override
    public void onRemoved() {
        idMap.remove(this);
        for (Entity entity : this.tracker.getAll()) {
            WorldManager.toCustomWorld(this.world).removeEntityFromTracker(entity, this);
            entity.remove();
        }
    }

    @Override
    public void onEntityHit(Entity entity, Player player, HandType currHand) {
        WorldManager.toCustomWorld(this.world).removeEntity(this);
    }

    @Override
    public void onEntityActivated(Entity entity, Player player, HandType currHand) {}

    @Override
    public void onEntityRemoved(Entity entity) {
        WorldManager.toCustomWorld(this.world).removeEntityFromTracker(entity, this);
    }

    private static Map<MonitorEntity, int[]> idMap = Maps.newHashMap();

    private static int unusedDimension() {
        return org.spongepowered.common.world.WorldManager.getNextFreeDimensionId();
    }

    private int[] reserveIdSpace(World world) {
        int[] ids = idMap.get(this);
        if (ids != null) {
            return ids;
        }
        ids = new int[this.numRows * this.numCols];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ImplUtil.getUniqueDataId(world, "map");
        }
        idMap.put(this, ids);
        return ids;
    }

    private FrameBuffer createInternalBuffer(World world) {
        int[] ids = reserveIdSpace(world);
        MapData[][] maps = new MapData[this.numRows][this.numCols];
        for (int i = 0; i < ids.length; i++) {
            int x = i % this.numCols;
            int y = i / this.numCols;
            MapData map = ImplUtil.loadMapData(world, "map_" + ids[i]);
            if (map == null) {
                map = new MapData("map_" + ids[i]);
                map.scale = 0;
                map.dimension = unusedDimension();
                map.markDirty();
                ImplUtil.setWorldData(world, "map_" + ids[i], map);
            }
            maps[y][x] = map;
        }
        return new MapBuffer(maps);
    }

    private static class MapBuffer implements FrameBuffer {
        private final MapData[][] maps;

        public MapBuffer(MapData[][] maps) {
            this.maps = maps;
        }

        private MapData getMap(int x, int y) {
            return this.maps[y / 128][x / 128];
        }

        private int index(int x, int y) {
            return ((y % 128) * 128) + (x % 128);
        }

        @Override
        public int getHeight() {
            return this.maps.length * 128;
        }

        @Override
        public int getWidth() {
            return this.maps[0].length * 128;
        }

        @Override
        public void clear(byte color) {
            for (int y = 0; y < this.maps.length; y++) {
                MapData[] row = this.maps[y];
                for (int x = 0; x < row.length; x++) {
                    Arrays.fill(row[x].colors, color);
                    row[x].updateMapData(0, 0);
                    row[x].updateMapData(127, 127);
                }
            }
        }

        @Override
        public byte getPixel(int x, int y) {
            return getMap(x, y).colors[index(x, y)];
        }

        @Override
        public void setPixel(int x, int y, byte pixel) {
            MapData map = getMap(x, y);
            int idx = index(x, y);
            if (map.colors[idx] != pixel) {
                map.colors[idx] = pixel;
                map.updateMapData(x % 128, y % 128);
            }
        }
    }

}
