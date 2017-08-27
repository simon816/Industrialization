package com.simon816.i15n.core;

import java.lang.reflect.Field;
import java.util.List;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.world.World;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.VecHelper;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.simon816.i15n.core.inv.InventoryAdapter;
import com.simon816.i15n.core.inv.impl.InternalInventoryWrapper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Because things are missing in 7.0.0
 */
public class ImplUtil {

    /**
     * {@link net.minecraft.world.World#playEvent} is not exposed.
     * 
     * @param world
     * @param pos
     * @param block
     * @param source
     */
    public static void playBlockBreakSound(World world, Vector3i pos, BlockState block, Player source) {
        ((net.minecraft.world.World) world).playEvent((EntityPlayer) source, 2001, VecHelper.toBlockPos(pos),
                Block.getStateId((IBlockState) block));
    }

    /**
     * {@link net.minecraft.item.ItemStack#splitStack} is not exposed. (though it's more of a
     * nice-to-have than a necessity).
     * 
     * @param original
     * @param count
     * @return
     */
    public static ItemStack splitItemStack(ItemStack original, int count) {
        return ItemStackUtil.fromNative(ItemStackUtil.toNative(original).splitStack(count));
    }

    /**
     * {@link Entity#isLoaded} doesn't actually check if it's loaded
     * 
     * @param entity
     * @return
     */
    public static boolean realIsLoaded(Entity entity) {
        return entity != null && !entity.isRemoved()
                && entity.getWorld().getEntity(entity.getUniqueId()).orElse(null) == entity;
    }

    public static void wrapInventory(Inventory inventory, InventoryAdapter wrap) {
        if (inventory instanceof CustomInventory) {
            try {
                Field fInv = CustomInventory.class.getDeclaredField("inv");
                fInv.setAccessible(true);
                InventoryBasic intInv = (InventoryBasic) fInv.get(inventory);
                fInv.set(inventory, new InternalInventoryWrapper.Basic(
                        intInv.getName(), intInv.hasCustomName(), intInv.getSizeInventory(), wrap));
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    public static int slotNumber(Slot slot) {
        return ((SlotAdapter) slot).getOrdinal();
    }

    public static Player containerToPlayer(Container container) {
        return (Player) ((InventoryPlayer) ((net.minecraft.inventory.Container) container)
                .getSlot(20).inventory).player;
    }

    public static void forceSlotUpdate(Container container, int slot, ItemStack stack) {
        EntityPlayerMP player = (EntityPlayerMP) containerToPlayer(container);
        player.connection.sendPacket(new SPacketSetSlot(
                ((net.minecraft.inventory.Container) container).windowId, slot, ItemStackUtil.toNative(stack)));
    }

    public static ItemStack findRecipe(InventoryAdapter inventory, int indexFrom, int indexTo, int offset,
            World world) {
        InventoryCrafting crafting = toCraftingMatrix(inventory, indexFrom, indexTo, offset);
        IRecipe recipe = CraftingManager.findMatchingRecipe(crafting, (net.minecraft.world.World) world);
        if (recipe == null) {
            return null;
        }
        return ItemStackUtil.fromNative(recipe.getCraftingResult(crafting));
    }

    public static List<ItemStack> getRemainingItems(InventoryAdapter inventory, int indexFrom, int indexTo, int offset,
            World world) {
        List<ItemStack> list = Lists.newArrayList();
        for (net.minecraft.item.ItemStack stack : CraftingManager.getRemainingItems(
                toCraftingMatrix(inventory, indexFrom, indexTo, offset),
                (net.minecraft.world.World) world)) {
            list.add(ItemStackUtil.fromNative(stack));
        }
        return list;
    }

    private static InventoryCrafting toCraftingMatrix(InventoryAdapter inventory, int indexFrom, int indexTo,
            int offset) {
        InventoryCrafting inv = new InventoryCrafting(FAKE_CONTAINER, 3, 3);
        for (int i = indexFrom; i <= indexTo; i++) {
            inv.setInventorySlotContents(i + offset, ItemStackUtil.toNative(inventory.getStack(i)));
        }
        return inv;
    }

    private static final net.minecraft.inventory.Container FAKE_CONTAINER = new net.minecraft.inventory.Container() {

        @Override
        public boolean canInteractWith(EntityPlayer playerIn) {
            return false;
        }
    };

    public static int getUniqueDataId(World world, String key) {
        return ((net.minecraft.world.World) world).getUniqueDataId(key);
    }

    public static MapData loadMapData(World world, String key) {
        return (MapData) ((net.minecraft.world.World) world).loadData(MapData.class, key);
    }

    public static void setWorldData(World world, String key, MapData mapdata) {
        ((net.minecraft.world.World) world).setData(key, mapdata);
    }

    public static void setItemDamage(ItemStack stack, int value) {
        ItemStackUtil.toNative(stack).setItemDamage(value);
    }

    // Sponge can't seem to handle registering recipies in POST_INIT
    public static void registerRecipe(ShapedCraftingRecipe recipe) {
        GameRegistry.findRegistry(IRecipe.class)
                .register(((IRecipe) recipe).setRegistryName(new ResourceLocation(recipe.getId())));
    }

}
