package com.simon816.i15n.core;

import static io.netty.buffer.Unpooled.buffer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.VecHelper;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Because things are missing in 4.2.0
 */
public class ImplUtil {

    /**
     * {@link Keys#INVISIBLE} is wrong.
     * 
     * @param entity
     * @param invisible
     */
    public static void setInvisible(Entity entity, boolean invisible) {
        ((net.minecraft.entity.Entity) entity).setInvisible(invisible);
    }

    /**
     * {@link net.minecraft.world.World#playAuxSFX} is not exposed.
     * 
     * @param world
     * @param pos
     * @param block
     */
    public static void playBlockBreakSound(World world, Vector3i pos, BlockState block) {
        ((net.minecraft.world.World) world).playAuxSFX(2001, VecHelper.toBlockPos(pos),
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
     * Recipes aren't implemented.
     * 
     * @param outputItem
     * @param params
     */
    public static void registerShapedRecipe(ItemStack outputItem, Object... params) {
        GameRegistry.addShapedRecipe(ItemStackUtil.toNative(outputItem), params);
    }

    /**
     * API method was removed.
     * 
     * @param itemEntity
     */
    public static void setInfinitePickupDelay(Item itemEntity) {
        try {
            Method m = itemEntity.getClass().getDeclaredMethod("setInfinitePickupDelay");
            m.invoke(itemEntity);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * API method was removed.
     * 
     * @param itemEntity
     */
    public static void setInfiniteDespawnTime(Item itemEntity) {
        try {
            Method m = itemEntity.getClass().getDeclaredMethod("setInfiniteDespawnTime");
            m.invoke(itemEntity);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * API method was removed.
     * 
     * @param itemEntity
     */
    public static void setPickupDelay(Item itemEntity, int delay) {
        try {
            Method m = itemEntity.getClass().getDeclaredMethod("setPickupDelay", int.class);
            m.invoke(itemEntity, delay);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * API method was removed.
     * 
     * @param itemEntity
     */
    public static void setDespawnTime(Item itemEntity, int time) {
        try {
            Method m = itemEntity.getClass().getDeclaredMethod("setDespawnTime", int.class);
            m.invoke(itemEntity, time);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
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

    static {
        try {
            Field fChannels = NetworkRegistry.class.getDeclaredField("channels");
            fChannels.setAccessible(true);
            @SuppressWarnings("unchecked")
            EnumMap<Side, Map<String, FMLEmbeddedChannel>> channels =
                    (EnumMap<Side, Map<String, FMLEmbeddedChannel>>) fChannels.get(NetworkRegistry.INSTANCE);
            Handler handler = new Handler();
            for (Side side : Side.values()) {
                FMLEmbeddedChannel channel = new FMLEmbeddedChannel("MC|BEdit", side, handler);
                channels.get(side).put("MC|BEdit", channel);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Sharable
    public static class Handler extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Consumer<List<String>> intercept = ctx.channel().attr(INTERCEPT_BOOK).get();
            ctx.channel().attr(INTERCEPT_BOOK).remove();
            if (msg instanceof FMLProxyPacket && intercept != null) {
                FMLProxyPacket packet = (FMLProxyPacket) msg;
                PacketBuffer buf = new PacketBuffer(packet.payload());
                net.minecraft.item.ItemStack item = buf.readItemStackFromBuffer();
                NBTTagCompound compound = item.getTagCompound();
                if (compound != null) {
                    NBTTagList pages = compound.getTagList("pages", NBT.TAG_STRING);
                    List<String> pageList = Lists.newArrayList();
                    for (int i = 0; i < pages.tagCount(); i++) {
                        pageList.add(pages.getStringTagAt(i));
                    }
                    intercept.accept(pageList);
                }
                return;
            }
            super.channelRead(ctx, msg);
        }
    }


    static final AttributeKey<Consumer<List<String>>> INTERCEPT_BOOK = AttributeKey.valueOf("intercept_book");

    public static void showWritableBook(Player player, List<String> lines, Consumer<List<String>> handler) {
        EntityPlayerMP internalPlayer = (EntityPlayerMP) player;
        NetHandlerPlayServer netHandler = internalPlayer.playerNetServerHandler;
        InventoryPlayer inventory = internalPlayer.inventory;
        int bookSlot = inventory.mainInventory.length + inventory.currentItem;
        net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(Items.writable_book);
        NBTTagList pages = new NBTTagList();
        for (String line : lines) {
            pages.appendTag(new NBTTagString(line));
        }
        item.setTagInfo("pages", pages);
        netHandler.sendPacket(new S2FPacketSetSlot(0, bookSlot, item));
        netHandler.sendPacket(new S3FPacketCustomPayload("MC|BOpen", new PacketBuffer(buffer())));
        NetworkRegistry.INSTANCE.getChannel("MC|BEdit", Side.SERVER).attr(INTERCEPT_BOOK).set(handler);
    }

}
