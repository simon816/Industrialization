package com.simon816.i15n.core.inv;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import com.simon816.i15n.core.Serialized;

/**
 * A stand-in replacement for SpongeAPI's Inventory until the API makes it easy to build custom
 * inventories.
 */
public interface InventoryAdapter extends Serialized {

    default SidedInventory asSided() {
        if (this instanceof SidedInventory) {
            return (SidedInventory) this;
        }
        return new SidedInventoryWrapper(this);
    }

    int getSize();

    ItemStack getStack(int index);

    void setStack(int index, ItemStack stack);

    ItemStack removeStack(int index);

    ItemStack decrementStack(int index, int count);

    boolean isEmpty();

    void clear();

    default int stackSizeLimit() {
        return 64;
    }

    Inventory getAPIInventory();

}
