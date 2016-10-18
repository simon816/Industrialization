package com.simon816.i15n.core.inv.impl;

import com.simon816.i15n.core.inv.InventoryAdapter;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public class InventoryCraftingWrapper extends InventoryCrafting implements InternalInventoryWrapper {

    private final InventoryAdapter inventory;

    public InventoryCraftingWrapper(InventoryAdapter inventory, int width, int height) {
        super(null, width, height);
        this.inventory = inventory;
    }

    @Override
    public InventoryAdapter getInventory() {
        return this.inventory;
    }

    @Override
    public int getSizeInventory() {
        return InternalInventoryWrapper.super.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return InternalInventoryWrapper.super.getStackInSlot(index);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return InternalInventoryWrapper.super.removeStackFromSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return InternalInventoryWrapper.super.decrStackSize(index, count);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        InternalInventoryWrapper.super.setInventorySlotContents(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return InternalInventoryWrapper.super.getInventoryStackLimit();
    }

    @Override
    public void clear() {
        InternalInventoryWrapper.super.clear();
    }
}
