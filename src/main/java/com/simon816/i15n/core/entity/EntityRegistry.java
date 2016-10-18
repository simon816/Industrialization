package com.simon816.i15n.core.entity;

import java.util.Map;

import org.spongepowered.api.world.World;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.simon816.i15n.core.SimpleRegistry;
import com.simon816.i15n.core.world.CustomWorld;

public class EntityRegistry {

    private static final Map<String, Function<CustomWorld, CustomEntity>> constructors = Maps.newHashMap();
    private static final SimpleRegistry<Class<? extends CustomEntity>> registry = new SimpleRegistry<>();

    public static void register(String id, Class<? extends CustomEntity> entityClass) {
        register(id, entityClass, constructorFunction(entityClass));
    }

    public static void register(String id, Class<? extends CustomEntity> entityClass,
            Function<CustomWorld, CustomEntity> constructorFunction) {
        registry.add(id, entityClass);
        constructors.put(id, constructorFunction);
    }

    private static Function<CustomWorld, CustomEntity> constructorFunction(Class<? extends CustomEntity> entityClass) {
        return world -> {
            try {
                return entityClass.getConstructor(World.class).newInstance(world.getWorld());
            } catch (ReflectiveOperationException | SecurityException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    public static String entityToId(Class<? extends CustomEntity> entityClass) {
        return registry.getKeyFor(entityClass);
    }

    public static void checkRegistered(Class<? extends CustomEntity> entityClass) {
        if (registry.getKeyFor(entityClass) == null) {
            throw new IllegalStateException("Entity class not registered! " + entityClass);
        }
    }

    public static CustomEntity construct(String id, CustomWorld world) {
        Function<CustomWorld, CustomEntity> fn = constructors.get(id);
        if (fn != null) {
            return fn.apply(world);
        }
        return null;
    }

}
