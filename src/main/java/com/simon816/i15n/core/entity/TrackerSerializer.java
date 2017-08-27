package com.simon816.i15n.core.entity;

import static org.spongepowered.api.data.DataQuery.of;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;

import com.google.common.collect.Maps;

public class TrackerSerializer {

    private Map<String, Entity> entities = Maps.newHashMap();

    public void add(String identifier, Entity entity) {
        this.entities.put(identifier, entity);
    }

    public Entity get(String identifier) {
        return this.entities.get(identifier);
    }

    public Collection<Entity> getAll() {
        return this.entities.values();
    }

    public void remove(String identifier) {
        this.entities.remove(identifier);
    }

    public void clear() {
        this.entities.clear();
    }

    public void readFrom(World world, DataView data) {
        DataView view = data.getView(of("trackedEntities")).get();
        for (DataQuery key : view.getKeys(false)) {
            String identifier = key.asString("");
            UUID entityUuid = view.getObject(key, UUID.class).orElse(null);
            if (entityUuid == null) {
                // Do nothing at the moment
            } else {
                Entity entity = world.getEntity(entityUuid).orElse(null);
                this.entities.put(identifier, entity);
            }
        }
    }

    public void writeTo(DataView data) {
        DataView view = data.createView(of("trackedEntities"));
        for (Map.Entry<String, Entity> entry : this.entities.entrySet()) {
            view.set(of(entry.getKey()), entry.getValue().getUniqueId());
        }
    }

}
