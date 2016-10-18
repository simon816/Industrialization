package com.simon816.i15n.core.entity;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.i15n.core.world.WorldManager;

public class EntityEventListeners {

    @Listener
    public void onEntityDestruct(DestructEntityEvent event) {
        Entity entity = event.getTargetEntity();
        CustomWorld world = WorldManager.toCustomWorld(entity.getWorld());
        EntityTracker tracker = world.getEntityTracker(event.getTargetEntity());
        if (tracker == null) {
            return;
        }
        tracker.onEntityRemoved(event.getTargetEntity());
    }

    @Listener
    public void onEntityInteract(InteractEntityEvent event, @First Player player) {
        CustomWorld world = WorldManager.toCustomWorld(event.getTargetEntity().getWorld());
        EntityTracker tracker = world.getEntityTracker(event.getTargetEntity());
        if (tracker == null) {
            return;
        }
        if (event instanceof InteractEntityEvent.Primary) {
            tracker.onEntityHit(event.getTargetEntity(), player);
        } else if (event instanceof InteractEntityEvent.Secondary) {
            tracker.onEntityActivated(event.getTargetEntity(), player);
        }
    }

}
