package com.simon816.i15n.core.inv.impl;

import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import com.simon816.i15n.core.inv.InventoryAdapter;
import com.simon816.i15n.core.inv.InventoryProvider;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public interface InternalInventoryWrapper extends IInventory, InventoryProvider {

    default ItemStack toNative(org.spongepowered.api.item.inventory.ItemStack stack) {
        return ItemStackUtil.toNative(stack);
    }

    default org.spongepowered.api.item.inventory.ItemStack toApi(ItemStack stack) {
        return ItemStackUtil.fromNative(stack);
    }

    @Override
    default int getSizeInventory() {
        return getInventory().getSize();
    }

    @Override
    default ItemStack getStackInSlot(int index) {
        return toNative(getInventory().getStack(index));
    }

    @Override
    default ItemStack removeStackFromSlot(int index) {
        return toNative(getInventory().removeStack(index));
    }

    @Override
    default ItemStack decrStackSize(int index, int count) {
        return toNative(getInventory().decrementStack(index, count));
    }

    @Override
    default void setInventorySlotContents(int index, ItemStack stack) {
        getInventory().setStack(index, toApi(stack));
    }

    @Override
    default int getInventoryStackLimit() {
        return getInventory().stackSizeLimit();
    }

    @Override
    default void clear() {
        getInventory().clear();
    }

    class Basic extends InventoryBasic implements InternalInventoryWrapper {

        private final InventoryAdapter inv;

        public Basic(String title, boolean customName, int slotCount, InventoryAdapter inv) {
            super(title, customName, slotCount);
            this.inv = inv;
        }

        @Override
        public InventoryAdapter getInventory() {
            return inv;
        }

        private boolean outOfRange(int index) {
            return index >= InternalInventoryWrapper.super.getSizeInventory();
        }

        @Override
        public ItemStack addItem(ItemStack stack) {
            // TODO Auto-generated method stub
            return super.addItem(stack);
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            if (outOfRange(index)) {
                return null;
            }
            return InternalInventoryWrapper.super.getStackInSlot(index);
        }

        @Override
        public ItemStack removeStackFromSlot(int index) {
            if (outOfRange(index)) {
                return null;
            }
            return InternalInventoryWrapper.super.removeStackFromSlot(index);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            if (outOfRange(index)) {
                return;
            }
            InternalInventoryWrapper.super.setInventorySlotContents(index, stack);
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {
            if (outOfRange(index)) {
                return null;
            }
            return InternalInventoryWrapper.super.decrStackSize(index, count);
        }
    }

}
