package com.simon816.i15n.core.item;

import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;

public class ItemWrench extends CustomItem {

    @Override
    public String getName() {
        return "Wrench";
    }

    @Override
    protected ItemType getVanillaItem() {
        return ItemTypes.BLAZE_ROD;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, Player player, BlockSnapshot clickedBlock, Direction side,
            Vector3d clickPoint) {
        return false;
    }

    public static boolean isPlayerUsing(Player player) {
        Optional<ItemStack> opItem = player.getItemInHand();
        if (!opItem.isPresent()) {
            return false;
        }
        CustomItem item = CustomItem.fromItemStack(opItem.get());
        return item == ItemRegistry.get("wrench");
    }

}
