package com.simon816.i15n.core.item;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;

public class CustomItemEventListeners {

    @Listener
    public void onBlockInteract(InteractBlockEvent.Secondary event, @First Player player) {
        Optional<ItemStack> opItem = player.getItemInHand();
        if (!opItem.isPresent() || !CustomItem.isCustomItem(opItem.get())) {
            return;
        }
        boolean useResult = CustomItem.fromItemStack(opItem.get()).onItemUse(opItem.get(), player,
                event.getTargetBlock(), event.getTargetSide(), event.getInteractionPoint().orElse(null));
        event.setCancelled(useResult);
    }

}
