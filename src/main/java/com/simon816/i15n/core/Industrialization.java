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
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
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
import com.simon816.i15n.core.entity.display.MonitorEntity;
import com.simon816.i15n.core.entity.turtle.TurtleEntity;
import com.simon816.i15n.core.inv.InventoryEventListeners;
import com.simon816.i15n.core.item.CustomItemEventListeners;
import com.simon816.i15n.core.item.ItemMonitor;
import com.simon816.i15n.core.item.ItemRegistry;
import com.simon816.i15n.core.item.ItemTurtle;
import com.simon816.i15n.core.item.ItemWrench;
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
        DataRegistration.builder()
                .dataClass(CustomItemData.class)
                .immutableClass(CustomItemData.Immutable.class)
                .builder(new CustomItemData.Builder())
                .manipulatorId("custom_item_data")
                .dataName("Custom Item Data")
                .buildAndRegister(toContainer());
    }

    private void registerObjects() {
        BlockRegistry.register("auto_crafting_bench", new AutoCraftingBench());
        ItemRegistry.registerItemForBlock(ItemTypes.CRAFTING_TABLE, BlockRegistry.get("auto_crafting_bench"));

        BlockRegistry.register("pipe", new PipeBlock());
        ItemRegistry.registerItemForBlock(ItemTypes.GLASS, BlockRegistry.get("pipe"));

        ItemRegistry.register("turtle", new ItemTurtle());
        EntityRegistry.register("turtle", TurtleEntity.class);

        ItemRegistry.register("wrench", new ItemWrench());

        ItemRegistry.register("monitor", new ItemMonitor());
        EntityRegistry.register("monitor", MonitorEntity.class, world -> new MonitorEntity(world.getWorld(), 0, 0));
    }

    private void registerRecipes() {
        ShapedCraftingRecipe.Builder builder = ShapedCraftingRecipe.builder();

        ShapedCraftingRecipe recipe;

        // @formatter:off
        recipe = builder.reset()
                .aisle("SSS",
                       "SCS",
                       "SSS")
                .where('S', Ingredient.of(ItemTypes.STICK))
                .where('C', Ingredient.of(ItemTypes.CRAFTING_TABLE))
                .result(ItemRegistry.get("auto_crafting_bench").createItemStack())
                .build("auto_crafting_bench", this);
        ImplUtil.registerRecipe(recipe);

        recipe = builder.reset()
                .aisle(" P ",
                       "P P",
                       " P ")
                .where('P', Ingredient.of(ItemTypes.GLASS_PANE))
                .result(ItemRegistry.get("pipe").createItemStack())
                .build("pipe", this);
        ImplUtil.registerRecipe(recipe);

        recipe = builder.reset()
                .aisle(" T ",
                       "IDP",
                       "   ")
                .where('T', Ingredient.of(ItemTypes.REDSTONE_TORCH))
                .where('I', Ingredient.of(ItemTypes.IRON_INGOT))
                .where('D', Ingredient.of(ItemTypes.DISPENSER))
                .where('P', Ingredient.of(ItemTypes.DIAMOND_PICKAXE))
                .result(ItemRegistry.get("turtle").createItemStack())
                .build("turtle", this);
        ImplUtil.registerRecipe(recipe);


        recipe = builder.reset()
                .aisle("ITI",
                       " S ",
                       " S ")
                .where('I', Ingredient.of(ItemTypes.IRON_INGOT))
                .where('T', Ingredient.of(ItemTypes.REDSTONE_TORCH))
                .where('S', Ingredient.of(ItemTypes.STICK))
                .result(ItemRegistry.get("wrench").createItemStack())
                .build("wrench", this);
        ImplUtil.registerRecipe(recipe);

        recipe = builder.reset()
                .aisle(" P ",
                       "PFP",
                       " P ")
                .where('P', Ingredient.of(ItemTypes.GLASS_PANE))
                .where('F', Ingredient.of(ItemTypes.ITEM_FRAME))
                .result(ItemRegistry.get("monitor").createItemStack())
                .build("monitor", this);
        ImplUtil.registerRecipe(recipe);

        // @formatter:on
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
        OutputStream stream = Files.newOutputStream(path, StandardOpenOption.WRITE);
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
