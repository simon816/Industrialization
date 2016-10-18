package com.simon816.i15n.core.inv;

import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.simon816.i15n.core.ImplUtil;
import com.simon816.i15n.core.Utils;

public class SimpleInventory implements InventoryAdapter {

    private ItemStack[] itemArray;

    public SimpleInventory(int size) {
        this.itemArray = new ItemStack[size];
    }

    @Override
    public int getSize() {
        return this.itemArray.length;
    }

    @Override
    public ItemStack getStack(int index) {
        return this.itemArray[index];
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        this.itemArray[index] = stack;
    }

    @Override
    public ItemStack removeStack(int index) {
        ItemStack stack = this.itemArray[index];
        this.itemArray[index] = null;
        return stack;
    }

    @Override
    public ItemStack decrementStack(int index, int count) {
        ItemStack original = this.itemArray[index];
        if (original == null) {
            return null;
        }
        if (original.getQuantity() <= count) {
            this.itemArray[index] = null;
            return original;
        } else {
            ItemStack rest = ImplUtil.splitItemStack(original, count);
            if (original.getQuantity() == 0) {
                this.itemArray[index] = null;
            }
            return rest;
        }

    }

    @Override
    public void clear() {
        this.itemArray = new ItemStack[this.itemArray.length];
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : this.itemArray) {
            if (item != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void writeTo(DataView data) {
        List<DataView> itemDataList = Lists.newArrayList();
        for (int i = 0; i < this.itemArray.length; i++) {
            ItemStack itemStack = this.itemArray[i];
            if (itemStack != null) {
                itemDataList.add(itemStack.toContainer());
            } else {
                itemDataList.add(Utils.emptyData());
            }
        }
        data.set(of("items"), itemDataList);
    }

    @Override
    public void readFrom(DataView data) {
        List<DataView> itemListData = data.getViewList(of("items")).get();
        this.itemArray = new ItemStack[itemListData.size()];
        for (int i = 0; i < itemListData.size(); i++) {
            this.itemArray[i] = Sponge.getDataManager().deserialize(ItemStack.class, itemListData.get(i)).orElse(null);
        }
    }

}
