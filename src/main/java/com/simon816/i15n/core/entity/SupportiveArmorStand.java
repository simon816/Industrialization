package com.simon816.i15n.core.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.cause.Cause;

import com.simon816.i15n.core.ImplUtil;

public class SupportiveArmorStand<T extends Entity> {

    private T entity;
    private ArmorStand stand;

    public SupportiveArmorStand() {}

    public boolean setEntity(T entity, boolean ensureStandCreated) {
        if (this.entity != null) {
            this.entity.remove();
        }
        this.entity = entity;
        if (entity == null) {
            if (this.stand != null) {
                this.stand.clearPassengers();
            }
            return false;
        }
        if (this.stand != null) {
            return this.entity.setVehicle(this.stand).isSuccessful();
        }
        return !ensureStandCreated || create();
    }

    public void setStand(ArmorStand stand) {
        if (this.stand != null) {
            this.stand.remove();
        }
        this.stand = stand;
        if (this.entity != null) {
            this.entity.setVehicle(stand);
        }
    }

    public boolean hasEntity() {
        return this.entity != null;
    }

    public boolean spawn(Cause cause) {
        if (!create()) {
            return false;
        }
        if (!ImplUtil.realIsLoaded(this.entity) && !this.entity.getWorld().spawnEntity(this.entity, cause)) {
            return false;
        }
        if (!ImplUtil.realIsLoaded(this.stand) && !this.entity.getWorld().spawnEntity(this.stand, cause)) {
            this.entity.remove();
            return false;
        }
        return true;
    }

    private boolean create() {
        if (this.entity == null) {
            return false;
        }
        if (this.stand != null) {
            // For some reason setPassenger behaves differently
            return this.entity.setVehicle(this.stand).isSuccessful();
        }
        this.stand = (ArmorStand) this.entity.getWorld().createEntity(EntityTypes.ARMOR_STAND,
                this.entity.getLocation().getPosition());
        if (!this.stand.offer(Keys.HAS_GRAVITY, false).isSuccessful()) {
            return false;
        }
        ArmorStandData data = this.stand.getOrCreate(ArmorStandData.class).get();
        data.set(data.basePlate().set(false));
        data.set(data.small().set(true));
        data.set(data.marker().set(true));
        if (!this.stand.offer(data).isSuccessful()) {
            return false;
        }
        this.stand.tryOffer(Keys.INVISIBLE, true);
        if (!this.stand.addPassenger(this.entity).isSuccessful()) {
            return false;
        }
        return true;
    }

    public void remove() {
        if (this.stand != null) {
            this.stand.remove();
        }
        if (this.entity != null) {
            this.entity.remove();
        }
    }

    public T getEntity() {
        return this.entity;
    }

    public ArmorStand getStand() {
        return this.stand;
    }

    public boolean isValid() {
        return ImplUtil.realIsLoaded(this.entity) && ImplUtil.realIsLoaded(this.stand);
    }

    @Override
    public String toString() {
        return "Supported{ " + this.entity + " by " + this.stand + " }";
    }

}
