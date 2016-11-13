package com.simon816.i15n.core.inv;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import com.simon816.i15n.core.inv.impl.InternalInventoryAdapter;

public class SpongeInventoryAdapter implements InventoryAdapter {

    public static InventoryAdapter forCarrier(Carrier carrier) {
        try {
            return new SpongeInventoryAdapter(carrier.getInventory());
        } catch (AbstractMethodError e) {
            e.printStackTrace();
            return InternalInventoryAdapter.from(carrier);
        }
    }

    private final Inventory inventory;

    public SpongeInventoryAdapter(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getAPIInventory() {
        return this.inventory;
    }

    @Override
    public int getSize() {
        return this.inventory.capacity();
    }

    @Override
    public ItemStack getStack(int index) {
        return this.inventory.peek().orElse(null);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        this.inventory.set(stack);
    }

    @Override
    public ItemStack removeStack(int index) {
        return this.inventory.poll().orElse(null);
    }

    @Override
    public ItemStack decrementStack(int index, int count) {
        return this.inventory.poll(count).orElse(null);
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.size() == 0;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public void readFrom(DataView data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeTo(DataView data) {
        // TODO Auto-generated method stub

    }
}
