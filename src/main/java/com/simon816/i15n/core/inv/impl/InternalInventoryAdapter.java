package com.simon816.i15n.core.inv.impl;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import com.simon816.i15n.core.inv.InventoryAdapter;

import net.minecraft.inventory.IInventory;

public class InternalInventoryAdapter implements InventoryAdapter {

    private final IInventory inventory;

    public InternalInventoryAdapter(IInventory inventory) {
        this.inventory = inventory;
    }

    private static net.minecraft.item.ItemStack toNative(ItemStack stack) {
        return ItemStackUtil.toNative(stack);
    }

    private static ItemStack toApi(net.minecraft.item.ItemStack stack) {
        return ItemStackUtil.fromNative(stack);
    }

    @Override
    public int getSize() {
        return this.inventory.getSizeInventory();
    }

    @Override
    public ItemStack getStack(int index) {
        return toApi(this.inventory.getStackInSlot(index));
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        this.inventory.setInventorySlotContents(index, toNative(stack));
    }

    @Override
    public ItemStack removeStack(int index) {
        return toApi(this.inventory.removeStackFromSlot(index));
    }

    @Override
    public ItemStack decrementStack(int index, int count) {
        return toApi(this.inventory.decrStackSize(index, count));
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getSize(); i++) {
            if (getStack(i) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int stackSizeLimit() {
        return this.inventory.getInventoryStackLimit();
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
