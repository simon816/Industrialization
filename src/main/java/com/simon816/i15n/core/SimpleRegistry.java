package com.simon816.i15n.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.simon816.i15n.compat.CatalogKey;

public class SimpleRegistry<T> {

    private final BiMap<CatalogKey, T> map = HashBiMap.create();

    public void add(CatalogKey key, T value) {
        if (this.map.putIfAbsent(key, value) != null) {
            throw new IllegalStateException(key + " is already registered");
        }
    }

    public T get(CatalogKey key) {
        return this.map.get(key);
    }

    public CatalogKey getKeyFor(T value) {
        return this.map.inverse().get(value);
    }

}
