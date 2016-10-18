package com.simon816.i15n.core.inv.impl;

import java.util.List;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import com.google.common.collect.Lists;
import com.simon816.i15n.core.tile.TileAutoCrafting;
import com.simon816.i15n.core.world.WorldManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.network.play.server.S2FPacketSetSlot;

public class ContainerAutoWorkbench extends ContainerWorkbench {

    private TileAutoCrafting tile;

    public ContainerAutoWorkbench(InventoryPlayer playerInventory, TileAutoCrafting tile) {
        super(playerInventory, null, null);
        this.tile = tile;
        this.craftMatrix = tile.getInternalInventory();
        // Re-maps all slots to the new craftMatrix
        List<Slot> newSlots = Lists.newArrayList();
        for (Slot slot : this.inventorySlots) {
            if (slot instanceof SlotCrafting) {
                slot = new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, slot.getSlotIndex(),
                        slot.xDisplayPosition, slot.yDisplayPosition) {
                    @Override
                    public void onPickupFromSlot(EntityPlayer playerIn, net.minecraft.item.ItemStack stack) {
                        if (!tile.canTakeOutput()) {
                            forceUpdateAll();
                            return;
                        }
                        tile.onPickup(true);
                        super.onPickupFromSlot(playerIn, stack);
                        tile.onPickup(false);
                    }
                };
            }
            if (slot.inventory instanceof InventoryCrafting) {
                slot = new Slot(this.craftMatrix, slot.getSlotIndex(), slot.xDisplayPosition, slot.yDisplayPosition);
            }
            newSlots.add(slot);
        }
        this.inventorySlots.clear();
        this.inventorySlots.addAll(newSlots);
    }

    void forceUpdateAll() {
        for (Slot slot : this.inventorySlots) {
            forceUpdate(slot.slotNumber);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return WorldManager.toCustomWorld((World) playerIn.worldObj).getBlockData(this.tile.getPosition()) == this.tile;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {}

    public void setOutput(ItemStack stack) {
        this.craftResult.setInventorySlotContents(0, ItemStackUtil.toNative(stack));
        detectAndSendChanges();
        forceUpdate(0);
    }

    private void forceUpdate(int slot) {
        for (ICrafting crafter : this.crafters) {
            if (crafter instanceof EntityPlayerMP) {
                ((EntityPlayerMP) crafter).playerNetServerHandler
                        .sendPacket(new S2FPacketSetSlot(this.windowId, slot, getSlot(slot).getStack()));
            }
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        // Copy of Container#onContainerClosed
        InventoryPlayer inventoryplayer = playerIn.inventory;
        if (inventoryplayer.getItemStack() != null) {
            playerIn.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), false);
            inventoryplayer.setItemStack(null);
        }
        this.tile.removeContainer(this);
    }

    public boolean onOutputClick() {
        return this.tile.canTakeOutput();
    }
}
