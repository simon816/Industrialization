package com.simon816.i15n.core.inv;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

public interface SidedInventory extends InventoryAdapter {

    boolean canPull(Direction side);

    ItemStack peek(Direction side);

    ItemStack pull(Direction side);

    boolean canPush(Direction side);

    boolean push(ItemStack stack, Direction side);

}
