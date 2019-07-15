package com.simon816.i15n.core.item;

import com.simon816.i15n.core.Utils;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;

public class CustomItemEventListeners {

    @Listener
    public void onBlockInteract(InteractBlockEvent.Secondary event, @First Player player) {
        HandType hand = Utils.getEventHand(event);
        ItemStack handItem = player.getItemInHand(hand).orElse(ItemStack.empty());
        if (handItem.isEmpty() || !CustomItem.isCustomItem(handItem)) {
            return;
        }
        boolean useResult = CustomItem.fromItemStack(handItem).onItemUse(handItem, player, hand,
                event.getTargetBlock(), event.getTargetSide(), event.getInteractionPoint().orElse(null));
        event.setCancelled(useResult);
    }

}
