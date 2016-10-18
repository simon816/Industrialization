package com.simon816.i15n.core.entity;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

public interface EntityTracker {

    default boolean acceptEntity(Entity entity) {
        return true;
    }

    void onEntityHit(Entity entity, Player player);

    void onEntityActivated(Entity entity, Player player);

    void onEntityRemoved(Entity entity);

    default void stopTracking(Entity entity) {}

}
