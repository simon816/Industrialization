package com.simon816.i15n.core.item;

import java.util.Optional;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.simon816.i15n.core.data.CustomItemData;

public abstract class CustomItem implements CatalogType {

    @Override
    public String getId() {
        return ItemRegistry.itemToId(this);
    }

    public ItemStack createItemStack() {
        ItemStack stack = ItemStack.of(getVanillaItem(), 1);
        CustomItemData customData = stack.getOrCreate(CustomItemData.class).get();
        applyItemStackData(customData.getData());
        stack.tryOffer(customData);
        applyItemStackModifiers(stack);
        return stack;
    }

    protected void applyItemStackData(DataView data) {
        data.set(DataQuery.of("id"), getId());
    }

    protected void applyItemStackModifiers(ItemStack stack) {
        stack.offer(Keys.DISPLAY_NAME, Text.of(getName()));
    }

    protected void readFromItemStack(ItemStack stack, DataView data) {}

    protected abstract ItemType getVanillaItem();

    private static DataView getData(ItemStack itemStack) {
        Optional<CustomItemData> opData = itemStack.get(CustomItemData.class);
        if (opData.isPresent()) {
            return opData.get().getData();
        }
        return null;
    }

    public boolean onItemUse(ItemStack itemStack, Player player, HandType currHand, BlockSnapshot clickedBlock,
            Direction side, Vector3d clickPoint) {
        return false;
    }

    public static boolean isCustomItem(ItemStack itemStack) {
        return getData(itemStack) != null;
    }

    public static CustomItem fromItemStack(ItemStack itemStack) {
        DataView data = getData(itemStack);
        Optional<String> opId;
        if (data == null || !(opId = data.getString(DataQuery.of("id"))).isPresent()) {
            return null;
        }
        CustomItem item = ItemRegistry.get(opId.get());
        if (item != null) {
            item.readFromItemStack(itemStack, data);
        }
        return item;
    }

}
