package com.simon816.i15n.core;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Iterables;
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
        return DataContainer.createNew();
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

    public static double directionToRotation(Direction direction) {
        if (direction == Direction.NORTH) {
            return 180;
        } else if (direction == Direction.EAST) {
            return 270;
        } else if (direction == Direction.SOUTH) {
            return 0;
        } else if (direction == Direction.WEST) {
            return 90;
        }
        return -1;
    }

    public static Iterable<Direction> cardinalDirections() {
        return Iterables.filter(Arrays.asList(Direction.values()), Direction::isCardinal);
    }

    public static Iterable<Direction> uprightDirections() {
        return Iterables.filter(Arrays.asList(Direction.values()), Direction::isUpright);
    }

    public static Iterable<Direction> blockFaces() {
        return Iterables.concat(cardinalDirections(), uprightDirections());
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

    public static HandType getEventHand(InteractEvent event) {
        HandType hand;
        if (event instanceof InteractBlockEvent.Primary.OffHand) {
            hand = HandTypes.OFF_HAND;
        } else if (event instanceof InteractBlockEvent.Secondary.OffHand) {
            hand = HandTypes.OFF_HAND;
        } else if (event instanceof InteractEntityEvent.Primary.OffHand) {
            hand = HandTypes.OFF_HAND;
        } else if (event instanceof InteractEntityEvent.Secondary.OffHand) {
            hand = HandTypes.OFF_HAND;
        } else {
            hand = HandTypes.MAIN_HAND;
        }
        return hand;
    }

}
