package com.simon816.i15n.automation.pipes;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import com.simon816.i15n.core.inv.InventoryAdapter;
import com.simon816.i15n.core.inv.SidedInventory;

public class InventoryPipeConnection implements IPipeConnectable {

    private final SidedInventory inv;

    public InventoryPipeConnection(InventoryAdapter inventory) {
        this.inv = inventory.asSided();
    }

    @Override
    public ItemStack pull(Direction side) {
        return this.inv.pull(side);
    }

    @Override
    public boolean push(ItemStack itemStack, Direction side) {
        return this.inv.push(itemStack, side);
    }

    public static IPipeConnectable from(InventoryAdapter inv) {
        if (inv == null) {
            return null;
        }
        if (inv instanceof IPipeConnectable) {
            return (IPipeConnectable) inv;
        }
        return new InventoryPipeConnection(inv);
    }

}
