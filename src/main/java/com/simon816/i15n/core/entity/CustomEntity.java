package com.simon816.i15n.core.entity;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.simon816.i15n.core.ITickable;
import com.simon816.i15n.core.Serialized;
import com.simon816.i15n.core.world.CustomWorld;


public abstract class CustomEntity implements CatalogType, ITickable, Serialized {

    protected Vector3d pos = Vector3d.ZERO;
    protected Vector3d rotation = Vector3d.ZERO;
    protected World world;

    public CustomEntity(World world) {
        EntityRegistry.checkRegistered(this.getClass());
        this.world = world;
    }

    @Override
    public CatalogKey getKey() {
        return EntityRegistry.entityToKey(this.getClass());
    }

    public Vector3d getPosition() {
        return this.pos;
    }

    public void setPosition(Vector3d pos) {
        this.pos = pos;
    }

    public Vector3d getRotation() {
        return this.rotation;
    }

    public void setRotation(Vector3d rot) {
        this.rotation = rot;
    }

    public void setPositionAndRotation(Vector3d pos, Vector3d rot) {
        setPosition(pos);
        setRotation(rot);
    }

    public World getWorld() {
        return this.world;
    }

    public abstract boolean spawnInWorld(CustomWorld world);

    public abstract void onRemoved();

    @Override
    public void readFrom(DataView data) {
        this.pos = data.getObject(of("position"), Vector3d.class).get();
        this.rotation = data.getObject(of("rotation"), Vector3d.class).get();
    }

    @Override
    public void writeTo(DataView data) {
        data.set(of("id"), getId());
        data.set(of("position"), this.pos);
        data.set(of("rotation"), this.rotation);
    }

}
