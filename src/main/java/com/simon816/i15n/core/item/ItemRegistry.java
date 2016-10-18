package com.simon816.i15n.core.item;

import org.spongepowered.api.item.ItemType;

import com.simon816.i15n.core.SimpleRegistry;
import com.simon816.i15n.core.block.BlockNature;

public class ItemRegistry {

    private static final SimpleRegistry<CustomItem> registry = new SimpleRegistry<>();

    public static void registerItemForBlock(ItemType vanillaItem, BlockNature block) {
        register(block.getId(), new ItemBlockWrapper(vanillaItem, block));
    }

    public static void register(String id, CustomItem item) {
        registry.add(id, item);
    }

    public static CustomItem get(String id) {
        return registry.get(id);
    }

    public static String itemToId(CustomItem item) {
        return registry.getKeyFor(item);
    }
}
