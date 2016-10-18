package com.simon816.i15n.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class SimpleRegistry<T> {
    private final BiMap<String, T> map = HashBiMap.create();

    public void add(String key, T value) {
        if (map.putIfAbsent(key, value) != null) {
            throw new IllegalStateException(key + " is already registered");
        }
    }

    public T get(String key) {
        return map.get(key);
    }

    public String getKeyFor(T value) {
        return map.inverse().get(value);
    }

}
