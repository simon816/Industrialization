package com.simon816.i15n.core.block;

import com.simon816.i15n.core.SimpleRegistry;

public class BlockRegistry {

    private static final SimpleRegistry<BlockNature> registry = new SimpleRegistry<>();

    public static void register(String id, BlockNature block) {
        registry.add(id, block);
    }

    public static BlockNature get(String id) {
        return registry.get(id);
    }

    public static String blockToId(BlockNature block) {
        return registry.getKeyFor(block);
    }

}
