package com.simon816.i15n.core;

import java.util.Map.Entry;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.data.DataList;
import com.simon816.i15n.core.inv.InventoryAdapter;
import com.simon816.i15n.core.inv.SpongeInventoryAdapter;

public class Utils {

    public static boolean ticksPassed(int n) {
        return Sponge.getServer().getRunningTimeTicks() % n == 0;
    }

    public static InventoryAdapter getInventory(World world, Vector3i pos) {
        Optional<TileEntity> opTe = world.getTileEntity(pos);
        if (opTe.isPresent()) {
            if (opTe.get() instanceof TileEntityCarrier) {
                return SpongeInventoryAdapter.forCarrier((TileEntityCarrier) opTe.get());
            }
        }
        return null;
    }

    public static DataList<DataView> createListView() {
        return new DataList<>(Utils::emptyData);
    }

    public static DataView emptyData() {
        return new MemoryDataContainer();
    }

    @SuppressWarnings("unchecked")
    public static <T> DataView s(T obj) {
        return Sponge.getDataManager().getSerializer((Class<T>) obj.getClass()).get().serialize(obj);
    }

    public static Direction rotationToDirection(double rotation) {
        rotation = Math.round(rotation);
        if (rotation < 45 || rotation > 315) {
            return Direction.SOUTH;
        }
        if (rotation < 135) {
            return Direction.WEST;
        }
        if (rotation < 225) {
            return Direction.NORTH;
        }
        return Direction.EAST;
    }

    public static void runLater(Runnable runnable) {
        Sponge.getScheduler().createTaskBuilder()
                .delayTicks(0).execute(runnable).submit(Industrialization.instance());
    }

    public static void merge(DataView first, DataView... others) {
        for (DataView other : others) {
            for (Entry<DataQuery, Object> valueEntry : other.getValues(true).entrySet()) {
                first.set(valueEntry.getKey(), valueEntry.getValue());
            }
        }
    }

}
