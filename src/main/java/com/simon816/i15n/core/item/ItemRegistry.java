package com.simon816.i15n.core.item;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.ItemType;

import com.simon816.i15n.core.I15NKey;
import com.simon816.i15n.core.SimpleRegistry;
import com.simon816.i15n.core.block.BlockNature;

public class ItemRegistry {

    private static final SimpleRegistry<CustomItem> registry = new SimpleRegistry<>();

    public static void registerItemForBlock(ItemType vanillaItem, BlockNature block) {
        register(block.getKey(), new ItemBlockWrapper(vanillaItem, block));
    }

    public static void register(CatalogKey key, CustomItem item) {
        registry.add(key, item);
    }

    public static CustomItem get(CatalogKey key) {
        return registry.get(key);
    }

    public static CustomItem get(String id) {
        return registry.get(new I15NKey("i15n", id));
    }

    public static CatalogKey itemToKey(CustomItem item) {
        return registry.getKeyFor(item);
    }
}
