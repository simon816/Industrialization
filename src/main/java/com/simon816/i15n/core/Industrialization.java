package com.simon816.i15n.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import com.google.inject.Inject;
import com.simon816.i15n.core.block.AutoCraftingBench;
import com.simon816.i15n.core.block.BlockRegistry;
import com.simon816.i15n.core.block.CustomBlockEventListeners;
import com.simon816.i15n.core.block.PipeBlock;
import com.simon816.i15n.core.data.CustomItemData;
import com.simon816.i15n.core.entity.EntityEventListeners;
import com.simon816.i15n.core.entity.EntityRegistry;
import com.simon816.i15n.core.entity.TurtleEntity;
import com.simon816.i15n.core.inv.InventoryEventListeners;
import com.simon816.i15n.core.item.CustomItemEventListeners;
import com.simon816.i15n.core.item.ItemRegistry;
import com.simon816.i15n.core.item.ItemTurtle;
import com.simon816.i15n.core.item.ItemWrench;
import com.simon816.i15n.core.recipe.RecipeRegistry;
import com.simon816.i15n.core.world.WorldEventListeners;
import com.simon816.i15n.core.world.WorldManager;

@Plugin(id = "industrialization", name = "Industrialization", version = "0.0.1")
public class Industrialization {

    private static Industrialization instance;

    @Inject
    private Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path confDir;

    public static Industrialization instance() {
        return instance;
    }

    public static PluginContainer toContainer() {
        return Sponge.getPluginManager().fromInstance(instance).get();
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        instance = this;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        registerEventListeners();
        registerData();
        registerObjects();
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        // Annoyingly, although sponge registers all data processors by preinit, they can't be
        // accessed until postinit (due to the delegates not being baked until then)
        registerRecipes();
    }

    private void registerData() {
        DataManager mgr = Sponge.getDataManager();
        mgr.register(CustomItemData.class, CustomItemData.Immutable.class, new CustomItemData.Builder());
    }

    private void registerObjects() {
        BlockRegistry.register("auto_crafting_bench", new AutoCraftingBench());
        ItemRegistry.registerItemForBlock(ItemTypes.CRAFTING_TABLE, BlockRegistry.get("auto_crafting_bench"));

        BlockRegistry.register("pipe", new PipeBlock());
        ItemRegistry.registerItemForBlock(ItemTypes.GLASS, BlockRegistry.get("pipe"));

        ItemRegistry.register("turtle", new ItemTurtle());
        EntityRegistry.register("turtle", TurtleEntity.class);

        ItemRegistry.register("wrench", new ItemWrench());
    }

    private void registerRecipes() {
        RecipeRegistry.builder(ItemRegistry.get("auto_crafting_bench"))
                .row("SSS")
                .row("SCS")
                .row("SSS")
                .replace('S', ItemTypes.STICK).replace('C', ItemTypes.CRAFTING_TABLE)
                .register();

        RecipeRegistry.builder(ItemRegistry.get("pipe"))
                .row(" P ")
                .row("P P")
                .row(" P ")
                .replace('P', ItemTypes.GLASS_PANE)
                .register();

        RecipeRegistry.builder(ItemRegistry.get("turtle"))
                .row(" T ")
                .row("IDP")
                .row("   ")
                .replace('T', ItemTypes.REDSTONE_TORCH).replace('I', ItemTypes.IRON_INGOT)
                .replace('D', ItemTypes.DISPENSER).replace('P', ItemTypes.DIAMOND_PICKAXE)
                .register();

        RecipeRegistry.builder(ItemRegistry.get("wrench"))
                .row("ITI")
                .row(" S ")
                .row(" S ")
                .replace('I', ItemTypes.IRON_INGOT).replace('T', ItemTypes.REDSTONE_TORCH)
                .replace('S', ItemTypes.STICK)
                .register();
    }

    private void registerEventListeners() {
        Sponge.getEventManager().registerListeners(this, new CustomItemEventListeners());
        Sponge.getEventManager().registerListeners(this, new CustomBlockEventListeners());
        Sponge.getEventManager().registerListeners(this, new InventoryEventListeners());
        Sponge.getEventManager().registerListeners(this, new EntityEventListeners());
        Sponge.getEventManager().registerListeners(this, new WorldEventListeners());
    }

    private Task tickTask;

    @Listener
    public void onServerStart(GameStartingServerEvent event) {
        this.tickTask = Sponge.getScheduler().createTaskBuilder()
                .execute(TickHelper::tick)
                .intervalTicks(1).submit(this);
    }

    public void loadDataForWorld(World world) throws IOException {
        Path path = getPathForWorld(world.getUniqueId());
        if (!Files.exists(path)) {
            return;
        }
        InputStream stream = Files.newInputStream(path, StandardOpenOption.READ);
        try {
            stream = new GZIPInputStream(stream);
            DataContainer data = DataFormats.NBT.readFrom(stream);
            WorldManager.loadWorld(world, data);
        } finally {
            stream.close();
        }
    }

    public void saveDataForWorld(World world) throws IOException {
        DataView data = Utils.emptyData();
        if (!WorldManager.saveWorld(world, data)) {
            return;
        }
        Path path = getPathForWorld(world.getUniqueId());
        OutputStream stream = Files.newOutputStream(path);
        try {
            stream = new GZIPOutputStream(stream);
            DataFormats.NBT.writeTo(stream, data);
        } finally {
            stream.close();
        }
    }

    private Path getPathForWorld(UUID worldUuid) {
        return this.confDir.resolve("world_" + worldUuid.toString() + ".dat");
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        this.tickTask.cancel();
    }

}
