package com.simon816.i15n.core.item;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.entity.display.MonitorEntity;
import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.i15n.core.world.WorldManager;

public class ItemMonitor extends CustomItem {

    @Override
    public String getName() {
        return "Computer Monitor";
    }

    @Override
    protected ItemType getVanillaItem() {
        return ItemTypes.ITEM_FRAME;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, Player player, HandType currHand, BlockSnapshot clickedBlock,
            Direction side, Vector3d clickPoint) {
        CustomWorld world = WorldManager.toCustomWorld(player.getWorld());
        Vector3i pos = clickedBlock.getPosition().add(side.asBlockOffset());
        MonitorEntity monitor = new MonitorEntity(player.getWorld(), 5, 4);
        monitor.setPosition(pos.toDouble());
        monitor.setRotation(new Vector3d(0, Utils.directionToRotation(side), 0));
        if (world.spawnEntity(monitor)) {
            if (player.gameMode().get() != GameModes.CREATIVE) {
                itemStack.setQuantity(itemStack.getQuantity() - 1);
                if (itemStack.getQuantity() == 0) {
                    itemStack = null;
                }
                player.setItemInHand(currHand, itemStack);
            }
        }

        return true;
    }

}
