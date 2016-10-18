package com.simon816.i15n.core;

import java.util.Set;

import com.google.common.collect.Sets;

public class TickHelper {

    private static final Set<Runnable> tickingObjects = Sets.newLinkedHashSet();
    private static final Set<Runnable> stagedAdd = Sets.newLinkedHashSet();
    private static final Set<Runnable> stagedRemove = Sets.newLinkedHashSet();
    private static boolean isTicking;

    public static void tick() {
        isTicking = true;
        for (Runnable runnable : tickingObjects) {
            runnable.run();
        }
        isTicking = false;
        tickingObjects.addAll(stagedAdd);
        tickingObjects.removeAll(stagedRemove);
        stagedAdd.clear();
        stagedRemove.clear();
    }

    public static void startTicking(Runnable runnable) {
        if (isTicking) {
            stagedAdd.add(runnable);
        } else {
            tickingObjects.add(runnable);
        }
    }

    public static void stopTicking(Runnable runnable) {
        if (isTicking) {
            stagedRemove.add(runnable);
        } else {
            tickingObjects.remove(runnable);
        }
    }
}
