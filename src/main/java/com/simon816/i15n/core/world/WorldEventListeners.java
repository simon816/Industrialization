package com.simon816.i15n.core.world;

import java.io.IOException;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;

import com.simon816.i15n.core.Industrialization;
import com.simon816.i15n.core.Utils;

public class WorldEventListeners {


    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        // Even though the world is loaded, it looks like nothing exists at this time.
        // Need to runLater in order for our stuff to hook into existing object etc.
        // The real solution is to hook into chunk load/unloads
        Utils.runLater(() -> {
            try {
                Industrialization.instance().loadDataForWorld(event.getTargetWorld());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Listener
    public void onWorldUnload(UnloadWorldEvent event) {
        try {
            Industrialization.instance().saveDataForWorld(event.getTargetWorld());
            WorldManager.unload(event.getTargetWorld());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
