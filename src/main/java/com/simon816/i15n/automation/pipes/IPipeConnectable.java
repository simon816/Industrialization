package com.simon816.i15n.automation.pipes;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

public interface IPipeConnectable {

    ItemStack pull(Direction side);

    boolean push(ItemStack itemStack, Direction side);

}
