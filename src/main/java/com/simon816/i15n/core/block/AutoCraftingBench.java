package com.simon816.i15n.core.block;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.util.VecHelper;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.inv.impl.ContainerAutoWorkbench;
import com.simon816.i15n.core.item.ItemBlockWrapper;
import com.simon816.i15n.core.tile.BlockData;
import com.simon816.i15n.core.tile.TileAutoCrafting;
import com.simon816.i15n.core.world.CustomWorld;

import net.minecraft.block.BlockWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.BlockPos;

public class AutoCraftingBench extends CustomBlock {

    @Override
    public String getName() {
        return "Auto Crafting Bench";
    }

    @Override
    public BlockData createData(CustomWorld world, Vector3i pos) {
        return new TileAutoCrafting(world, pos);
    }

    @Override
    public boolean onBlockBreak(CustomWorld world, Vector3i pos, Player player) {
        System.out.println("AutoCraftingBench.onBlockBreak()");
        // TODO Auto-generated method stub
        return super.onBlockBreak(world, pos, player);
    }

    @Override
    public boolean onBlockHit(CustomWorld world, Vector3i pos, Player player, Direction side, Vector3d clickPoint) {
        System.out.println("AutoCraftingBench.onBlockHit()"); // TODO Auto-generated method stub
        return super.onBlockHit(world, pos, player, side, clickPoint);
    }

    @Override
    public boolean onNeighborNotify(CustomWorld world, Vector3i pos, Vector3i neighbourPos, Direction side) {
        System.out.println("AutoCraftingBench.onNeighborNotify()"); // TODO Auto-generated method
                                                                    // stub
        return super.onNeighborNotify(world, pos, neighbourPos, side);
    }

    @Override
    public boolean onBlockPlacedByPlayer(CustomWorld world, Vector3i pos, Player player, ItemBlockWrapper item,
            ItemStack itemStack) {
        System.out.println("AutoCraftingBench.onBlockPlacedByPlayer()"); // TODO Auto-generated
                                                                         // method stub
        return super.onBlockPlacedByPlayer(world, pos, player, item, itemStack);
    }

    @Override
    public boolean onBlockActivated(CustomWorld world, Vector3i pos, Player player, Direction side,
            Vector3d clickPoint) {
        System.out.println("AutoCraftingBench.onBlockActivated()");
        BlockData te = world.getBlockData(pos);
        showGui((EntityPlayerMP) player, (net.minecraft.world.World) world.getWorld(), pos, (TileAutoCrafting) te);
        return false;
    }

    private void showGui(EntityPlayerMP player, net.minecraft.world.World world, Vector3i position,
            TileAutoCrafting te) {
        BlockPos pos = VecHelper.toBlockPos(position);
        player.displayGui(new BlockWorkbench.InterfaceCraftingTable(world, pos) {
            @Override
            public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
                ContainerAutoWorkbench container = new ContainerAutoWorkbench(playerInventory, te);
                te.addContainer(container);
                return container;
            }
        });
    }

}
