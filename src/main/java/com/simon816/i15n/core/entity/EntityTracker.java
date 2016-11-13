package com.simon816.i15n.core.entity;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

public interface EntityTracker {

    default boolean acceptEntity(Entity entity) {
        return true;
    }

    void onEntityHit(Entity entity, Player player, HandType currHand);

    void onEntityActivated(Entity entity, Player player, HandType currHand);

    void onEntityRemoved(Entity entity);

    default void stopTracking(Entity entity) {}

}
