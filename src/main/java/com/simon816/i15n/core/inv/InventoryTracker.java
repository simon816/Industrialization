package com.simon816.i15n.core.inv;

import java.util.Map;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.TargetInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class InventoryTracker {

    private static final Map<Inventory, EventBinder> tracked = Maps.newHashMap();

    public static EventBinder startTracking(Inventory inv) {
        EventBinder binder = new EventBinder();
        tracked.put(inv, binder);
        return binder;
    }

    static EventBinder get(Inventory inv) {
        return tracked.get(inv);
    }

    public static void stopTracking(Inventory inv) {
        tracked.remove(inv);
    }


    public static class EventBinder {

        private final Multimap<Class<?>, EventListener<Event>> handlers = HashMultimap.create();

        void incommingEvent(TargetInventoryEvent event) {
            handleEvent(event.getClass().getInterfaces(), event);
            handleEvent(event.getClass(), event);
        }

        private void handleEvent(Class<?>[] cs, TargetInventoryEvent e) {
            for (Class<?> c : cs) {
                handleEvent(c.getInterfaces(), e);
                handleEvent(c, e);
            }
        }

        private void handleEvent(Class<?> c, TargetInventoryEvent e) {
            this.handlers.get(c).forEach(h -> {
                try {
                    h.handle(e);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        }

        @SuppressWarnings("unchecked")
        public <T extends TargetInventoryEvent> void on(Class<T> eventClass, EventListener<T> handler) {
            this.handlers.put(eventClass, (EventListener<Event>) (Object) handler);
        }
    }
}
