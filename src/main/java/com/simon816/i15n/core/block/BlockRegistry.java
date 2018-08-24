package com.simon816.i15n.core.block;

import org.spongepowered.api.CatalogKey;

import com.simon816.i15n.core.I15NKey;
import com.simon816.i15n.core.SimpleRegistry;

public class BlockRegistry {

    private static final SimpleRegistry<BlockNature> registry = new SimpleRegistry<>();

    public static void register(CatalogKey key, BlockNature block) {
        registry.add(key, block);
    }

    public static BlockNature get(CatalogKey key) {
        return registry.get(key);
    }

    public static BlockNature get(String id) {
        return registry.get(new I15NKey("i15n", id));
    }

    public static CatalogKey blockToKey(BlockNature block) {
        return registry.getKeyFor(block);
    }

}
