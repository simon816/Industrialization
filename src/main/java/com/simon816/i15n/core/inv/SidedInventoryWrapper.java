package com.simon816.i15n.core.inv;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

public class SidedInventoryWrapper extends InventoryDelegate implements SidedInventory {

    public SidedInventoryWrapper(InventoryAdapter inventory) {
        super(inventory);
    }

    @Override
    public boolean canPull(Direction side) {
        return true;
    }

    @Override
    public boolean canPush(Direction side) {
        return true;
    }

    @Override
    public ItemStack peek(Direction side) {
        int index = nonEmpty();
        if (index == -1) {
            return null;
        }
        return getStack(index);
    }

    @Override
    public ItemStack pull(Direction side) {
        int index = nonEmpty();
        if (index == -1) {
            return null;
        }
        // TODO remove or decrement
        return decrementStack(index, 1);
    }

    @Override
    public boolean push(ItemStack stack, Direction side) {
        int index = nextEmpty();
        if (index == -1) {
            return false;
        }
        setStack(index, stack);
        return true;
    }

    private int nextEmpty() {
        int size = getSize();
        for (int i = 0; i < size; i++) {
            if (getStack(i) == null) {
                return i;
            }
        }
        return -1;
    }

    private int nonEmpty() {
        int size = getSize();
        for (int i = 0; i < size; i++) {
            if (getStack(i) != null) {
                return i;
            }
        }
        return -1;
    }
}
