package com.simon816.i15n.core.item;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.simon816.i15n.core.block.BlockNature;
import com.simon816.i15n.core.block.CustomBlockEventListeners;
import com.simon816.i15n.core.tile.BlockData;

public class ItemBlockWrapper extends CustomItem {

    private final ItemType vanillaItem;
    private final BlockNature block;

    public ItemBlockWrapper(ItemType vanillaItem, BlockNature block) {
        this.vanillaItem = vanillaItem;
        this.block = block;
    }

    @Override
    public String getName() {
        return this.block.getName();
    }

    @Override
    protected ItemType getVanillaItem() {
        return this.vanillaItem;
    }

    public BlockNature getBlock() {
        return this.block;
    }

    /*
     * Linked to CustomBlockEventListeners#onBlockPlaceAfterItemUse
     * 
     * Tells the block event counterpart that this itemblock has been 'used'.
     * 
     * Tells vanilla to use default behaviour so block placement works.
     */
    @Override
    public boolean onItemUse(ItemStack itemStack, Player player, HandType currHand, BlockSnapshot clickedBlock,
            Direction side, Vector3d clickPoint) {
        CustomBlockEventListeners.setItemBlockClicked(itemStack, player);
        return false; // Pass through to vanilla
    }

    public void transferToBlock(ItemStack itemStack, BlockData blockData) {}

}
