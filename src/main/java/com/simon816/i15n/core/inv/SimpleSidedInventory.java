package com.simon816.i15n.core.inv;

import java.util.EnumMap;
import java.util.function.BiPredicate;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import com.google.common.collect.Maps;

public class SimpleSidedInventory extends InventoryDelegate implements SidedInventory {

    public interface PullHandler {
        ItemStack peek(Direction side);

        ItemStack pull(Direction side);
    }

    private final EnumMap<Direction, PullHandler> pullHandlers = Maps.newEnumMap(Direction.class);
    private final EnumMap<Direction, BiPredicate<ItemStack, Direction>> pushHandlers = Maps.newEnumMap(Direction.class);

    public SimpleSidedInventory(InventoryAdapter inv) {
        super(inv);
    }

    @Override
    public SidedInventory asSided() {
        return this;
    }

    public void setPushHandler(BiPredicate<ItemStack, Direction> handler, Direction... sides) {
        for (Direction side : sides) {
            this.pushHandlers.put(side, handler);
        }
    }

    public void setPullHandler(PullHandler handler, Direction... sides) {
        for (Direction side : sides) {
            this.pullHandlers.put(side, handler);
        }
    }

    @Override
    public boolean canPull(Direction side) {
        return this.pullHandlers.containsKey(side);
    }

    @Override
    public boolean canPush(Direction side) {
        return this.pushHandlers.containsKey(side);
    }

    @Override
    public ItemStack peek(Direction side) {
        if (!canPull(side)) {
            return null;
        }
        return this.pullHandlers.get(side).peek(side);
    }

    @Override
    public ItemStack pull(Direction side) {
        if (!canPull(side)) {
            return null;
        }
        return this.pullHandlers.get(side).pull(side);
    }

    @Override
    public boolean push(ItemStack stack, Direction side) {
        if (!canPush(side)) {
            return false;
        }
        return this.pushHandlers.get(side).test(stack, side);
    }

}
