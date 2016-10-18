package com.simon816.i15n.core;

public interface ITickable extends Runnable {

    void tick();

    @Override
    default void run() {
        tick();
    }
}
