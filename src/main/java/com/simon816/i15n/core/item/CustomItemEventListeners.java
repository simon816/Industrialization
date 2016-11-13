package com.simon816.i15n.core.item;

import java.util.Optional;

import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;

import com.simon816.i15n.core.Utils;

public class CustomItemEventListeners {

    @Listener
    public void onBlockInteract(InteractBlockEvent.Secondary event, @First Player player) {
        HandType hand = Utils.getEventHand(event);
        Optional<ItemStack> opItem = player.getItemInHand(hand);
        if (!opItem.isPresent() || !CustomItem.isCustomItem(opItem.get())) {
            return;
        }
        boolean useResult = CustomItem.fromItemStack(opItem.get()).onItemUse(opItem.get(), player, hand,
                event.getTargetBlock(), event.getTargetSide(), event.getInteractionPoint().orElse(null));
        event.setCancelled(useResult);
    }

}
