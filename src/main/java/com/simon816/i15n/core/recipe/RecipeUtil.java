package com.simon816.i15n.core.recipe;

import java.util.List;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;

import com.simon816.i15n.core.ImplUtil;
import com.simon816.i15n.core.inv.InventoryAdapter;

public class RecipeUtil {

    public static ItemStack findRecipe(InventoryAdapter inventory, int indexFrom, int indexTo, int offset,
            World world) {
        return ImplUtil.findRecipe(inventory, indexFrom, indexTo, offset, world);
    }

    public static List<ItemStack> getRemainingItems(InventoryAdapter inventory, int indexFrom, int indexTo, int offset,
            World world) {
        return ImplUtil.getRemainingItems(inventory, indexFrom, indexTo, offset, world);
    }

}
