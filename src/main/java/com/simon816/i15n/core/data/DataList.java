package com.simon816.i15n.core.data;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

public class DataList<T> {

    private final Supplier<T> suppier;
    private final List<T> list = Lists.newArrayList();

    public DataList(Supplier<T> supplier) {
        this.suppier = supplier;
    }

    public T next() {
        T obj = this.suppier.get();
        this.list.add(obj);
        return obj;
    }

    public List<T> getList() {
        return this.list;
    }
}
