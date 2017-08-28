package com.simon816.i15n.core.cpu;

public interface Register {

    int get();

    void set(int value);

    default int getAndInc() {
        int val = get();
        inc();
        return val;
    }

    default void inc() {
        set(get() + 1);
    }

    default int getbit(int idx) {
        return (get() >>> (idx + 1)) & 1;
    }

    default void setbit(int idx, boolean set) {
        int val = get();
        set(set ? (val | 1 << idx) : (val & ~(1 << idx)));
    }

    int max();

    int negBit();

}
