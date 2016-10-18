package com.simon816.i15n.core.inv.impl;

import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import com.simon816.i15n.core.inv.InventoryAdapter;
import com.simon816.i15n.core.inv.InventoryProvider;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

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

    class Simple implements InternalInventoryWrapper {

        private final InventoryAdapter inv;

        public Simple(InventoryAdapter inv) {
            this.inv = inv;
        }

        @Override
        public InventoryAdapter getInventory() {
            return this.inv;
        }

        @Override
        public int getSizeInventory() {
            int size = InternalInventoryWrapper.super.getSizeInventory();
            while (size % 9 != 0) {
                size++;
            }
            return size;
        }


        private boolean outOfRange(int index) {
            return index >= InternalInventoryWrapper.super.getSizeInventory();
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

        @Override
        public void markDirty() {}

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return true;
        }

        @Override
        public void openInventory(EntityPlayer player) {}

        @Override
        public void closeInventory(EntityPlayer player) {}

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return true;
        }

        @Override
        public int getField(int id) {
            return 0;
        }

        @Override
        public void setField(int id, int value) {}

        @Override
        public int getFieldCount() {
            return 0;
        }

        @Override
        public String getName() {
            return "container.chest";
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Override
        public IChatComponent getDisplayName() {
            return hasCustomName() ? new ChatComponentText(getName()) : new ChatComponentTranslation(getName());
        }
    }
}
