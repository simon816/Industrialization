package com.simon816.i15n.core.inv;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

public class InventoryDelegate implements InventoryAdapter {

    protected final InventoryAdapter inv;

    public InventoryDelegate(InventoryAdapter inv) {
        this.inv = inv;
    }

    @Override
    public SidedInventory asSided() {
        return this.inv.asSided();
    }

    @Override
    public Inventory getAPIInventory() {
        return this.inv.getAPIInventory();
    }

    @Override
    public int getSize() {
        return this.inv.getSize();
    }

    @Override
    public ItemStack getStack(int index) {
        return this.inv.getStack(index);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        this.inv.setStack(index, stack);
    }

    @Override
    public ItemStack removeStack(int index) {
        return this.inv.removeStack(index);
    }

    @Override
    public ItemStack decrementStack(int index, int count) {
        return this.inv.decrementStack(index, count);
    }

    @Override
    public boolean isEmpty() {
        return this.inv.isEmpty();
    }

    @Override
    public void clear() {
        this.inv.clear();
    }

    @Override
    public void readFrom(DataView data) {
        this.inv.readFrom(data);
    }

    @Override
    public void writeTo(DataView data) {
        this.inv.writeTo(data);
    }
}
