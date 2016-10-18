package com.simon816.i15n.core.inv;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import com.simon816.i15n.core.inv.impl.ContainerAutoWorkbench;

public class InventoryEventListeners {

    @Listener
    public void onInventoryClick(ClickInventoryEvent event) {
        if (event.getTargetInventory() instanceof ContainerAutoWorkbench) {
            for (SlotTransaction transaction : event.getTransactions()) {
                if (!transaction.isValid()) {
                    continue;
                }
                if (transaction.getOriginal().getCount() < 1) {
                    if (!((ContainerAutoWorkbench) event.getTargetInventory()).onOutputClick()) {
                        transaction.setValid(false);
                        transaction.setCustom(transaction.getOriginal());
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

}
