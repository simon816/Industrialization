package com.simon816.i15n.core.entity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.simon816.i15n.core.Serialized;
import com.simon816.i15n.core.block.BlockNature;
import com.simon816.i15n.core.world.AdditionalBlockInfo;
import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.i15n.core.world.WorldManager;

public class MultiEntityStructure {

    public static class Builder {

        private Map<String, BiFunction<World, Vector3i, Entity>> definitions = Maps.newHashMap();

        public MultiEntityStructure build() {
            return new MultiEntityStructure(this.definitions);
        }

        public <T extends Entity> Builder define(String identifier, EntityType entityType, Vector3f offsetPos,
                Class<T> entityTypeClass,
                Consumer<T> initializer) {
            if (!entityTypeClass.isAssignableFrom(entityType.getEntityClass())) {
                throw new IllegalArgumentException("Incorrect class for type");
            }
            this.definitions.put(identifier, (world, pos) -> {
                @SuppressWarnings("unchecked")
                T entity = (T) world.createEntity(entityType, pos.toFloat().add(offsetPos).toDouble());
                initializer.accept(entity);
                return entity;
            });
            return this;
        }


    }

    final ImmutableMap<String, BiFunction<World, Vector3i, Entity>> definitions;

    MultiEntityStructure(Map<String, BiFunction<World, Vector3i, Entity>> definitions) {
        this.definitions = ImmutableMap.copyOf(definitions);
    }

    private static Entity spawn(BiFunction<World, Vector3i, Entity> fn, World world, Vector3i pos) {
        Entity entity = fn.apply(world, pos);
        if (entity != null && world.spawnEntity(entity)) {
            return entity;
        }
        return null;
    }

    private StructureInstance add(CustomWorld world, Vector3i pos, Map<String, Entity> entityMap) {
        StructureInstance struct = new StructureInstance(world.getWorld(), pos, entityMap);
        for (Entity entity : entityMap.values()) {
            if (entity != null) {
                world.addEntityToTracker(entity, struct);
            }
        }
        world.setBlockInfo(pos, struct);
        return struct;
    }

    public StructureInstance initialize(CustomWorld world, Vector3i pos) {
        Map<String, Entity> entityMap = Maps.newHashMap();
        for (Entry<String, BiFunction<World, Vector3i, Entity>> entry : this.definitions.entrySet()) {
            Entity entity = spawn(entry.getValue(), world.getWorld(), pos);
            if (entity != null) {
                entityMap.put(entry.getKey(), entity);
            }
        }
        return add(world, pos, entityMap);
    }

    public StructureInstance initFromData(CustomWorld world, Vector3i pos, DataView data) {
        Map<String, Entity> entityMap = Maps.newHashMap();
        for (DataQuery key : data.getKeys(false)) {
            String identifier = key.asString("");
            UUID entityUuid = data.getObject(key, UUID.class).orElse(null);
            if (entityUuid == null) { // Manually removed from previous session
                if (this.definitions.containsKey(identifier)) {
                    // Only retain permanent entities (ignore late bound)
                    entityMap.put(identifier, null);
                }
            } else {
                Entity existingEntity = world.getWorld().getEntity(entityUuid).orElse(null);
                if (existingEntity != null) {
                    entityMap.put(identifier, existingEntity);
                }
            }
        }
        for (Entry<String, BiFunction<World, Vector3i, Entity>> entry : this.definitions.entrySet()) {
            if (entityMap.containsKey(entry.getKey())) {
                continue;
            }
            Entity entity = spawn(entry.getValue(), world.getWorld(), pos);
            if (entity != null) {
                entityMap.put(entry.getKey(), entity);
            }
        }
        return add(world, pos, entityMap);
    }

    public static class StructureInstance implements Serialized, EntityTracker, AdditionalBlockInfo {

        private final World world;
        private final Vector3i pos;
        private final Map<String, Entity> entities;

        StructureInstance(World world, Vector3i pos, Map<String, Entity> entityMap) {
            this.world = world;
            this.pos = pos;
            this.entities = entityMap;
        }

        public void remove() {
            CustomWorld world = WorldManager.toCustomWorld(this.world);
            for (Entity entity : this.entities.values()) {
                if (entity != null) {
                    entity.remove();
                    world.removeEntityFromTracker(entity, this);
                }
            }
        }

        public Entity get(String identifier) {
            return this.entities.get(identifier);
        }

        public void remove(String identifier) {
            boolean existed = this.entities.containsKey(identifier);
            Entity e = this.entities.remove(identifier);
            if (existed) {
                this.entities.put(identifier, null); // Mark as removed
            }
            if (e != null) {
                WorldManager.toCustomWorld(this.world).removeEntityFromTracker(e, this);
                e.remove();
            }
        }

        public void forEachEntity(Consumer<Entity> func) {
            this.entities.values().forEach(func);
        }

        public void forEachEntity(BiConsumer<String, Entity> func) {
            this.entities.entrySet().forEach(e -> func.accept(e.getKey(), e.getValue()));
        }

        public <T extends Entity> boolean lateBind(String identifier, EntityType type, Vector3f offsetPos,
                Class<T> entityClass,
                Consumer<T> initializer) {
            @SuppressWarnings("unchecked")
            T entity = (T) this.world.createEntity(type, this.pos.toFloat().add(offsetPos).toDouble());
            initializer.accept(entity);
            return spawn(identifier, entity);
        }

        private boolean spawn(String identifier, Entity entity) {
            if (this.world.spawnEntity(entity)) {
                remove(identifier); // In case of existing ones
                this.entities.put(identifier, entity);
                WorldManager.toCustomWorld(this.world).addEntityToTracker(entity, this);
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Structure(" + this.pos + ")" + this.entities.toString();
        }

        @Override
        public void onEntityActivated(Entity entity, Player player, HandType currHand) {}

        @Override
        public void onEntityHit(Entity entity, Player player, HandType currHand) {}

        @Override
        public void onEntityRemoved(Entity entity) {
            CustomWorld w = WorldManager.toCustomWorld(this.world);
            BlockNature block = w.getBlock(this.pos);
            if (block != null) {
                block.onBlockBreak(w, this.pos, null);
            }
        }

        public void recreate(MultiEntityStructure struct, String identifier) {
            if (this.entities.get(identifier) != null) {
                return;
            }
            BiFunction<World, Vector3i, Entity> init = struct.definitions.get(identifier);
            if (init != null) {
                Entity entity = init.apply(this.world, this.pos);
                if (entity != null) {
                    spawn(identifier, entity);
                }
            }
        }

        @Override
        public void readFrom(DataView data) {
            throw new UnsupportedOperationException("Use MultiEntityStructure#initFromData");
        }

        @Override
        public void writeTo(DataView data) {
            for (Entry<String, Entity> entry : this.entities.entrySet()) {
                Object uuid = entry.getValue() != null ? entry.getValue().getUniqueId() : 0;
                data.set(of(entry.getKey().toString()), uuid);
            }
        }
    }

}
