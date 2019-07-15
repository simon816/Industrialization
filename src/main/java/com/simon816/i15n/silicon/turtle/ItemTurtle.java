package com.simon816.i15n.silicon.turtle;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3d;
import com.simon816.i15n.core.item.CustomItem;
import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.i15n.core.world.WorldManager;

public class ItemTurtle extends CustomItem {

    @Override
    public String getName() {
        return "Turtle";
    }

    @Override
    protected ItemType getVanillaItem() {
        return ItemTypes.DISPENSER;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, Player player, HandType currHand, BlockSnapshot clickedBlock,
            Direction side, Vector3d hitPoint) {
        CustomWorld world = WorldManager.toCustomWorld(player.getWorld());
        Vector3d pos;
        if (hitPoint == null) {
            pos = clickedBlock.getPosition().add(side.asBlockOffset()).toDouble().add(0.5, 0, 0.5);
        } else {
            pos = clickedBlock.getPosition().toDouble().add(hitPoint);
        }
        TurtleEntity turtle = new TurtleEntity(player.getWorld());
        turtle.setPosition(pos);
        Vector3d r = player.getHeadRotation();
        turtle.setRotation(new Vector3d(r.getX(), r.getY() + 180, r.getZ()));
        if (world.spawnEntity(turtle)) {
            if (player.gameMode().get() != GameModes.CREATIVE) {
                itemStack.setQuantity(itemStack.getQuantity() - 1);
                if (itemStack.getQuantity() == 0) {
                    itemStack = null;
                }
                player.setItemInHand(currHand, itemStack);
            }
        }
        return false;
    }

}
