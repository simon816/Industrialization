package com.simon816.i15n.core.inv;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.TargetInventoryEvent;

import com.simon816.i15n.core.inv.InventoryTracker.EventBinder;

public class InventoryEventListeners {

    @Listener
    public void onInventoryClick(ClickInventoryEvent event) {}

    @Listener
    public void onInventoryEvent(TargetInventoryEvent event) {
        EventBinder binder = InventoryTracker.get(event.getTargetInventory());
        if (binder != null) {
            binder.incommingEvent(event);
        }
    }

}
