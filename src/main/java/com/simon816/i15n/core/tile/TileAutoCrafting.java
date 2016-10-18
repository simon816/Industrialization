package com.simon816.i15n.core.tile;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.simon816.i15n.core.ITickable;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.inv.InventoryAdapter;
import com.simon816.i15n.core.inv.InventoryProvider;
import com.simon816.i15n.core.inv.ObservableInventory;
import com.simon816.i15n.core.inv.ObservableInventory.SimpleObserver;
import com.simon816.i15n.core.inv.SimpleInventory;
import com.simon816.i15n.core.inv.SimpleSidedInventory;
import com.simon816.i15n.core.inv.SimpleSidedInventory.PullHandler;
import com.simon816.i15n.core.inv.impl.ContainerAutoWorkbench;
import com.simon816.i15n.core.inv.impl.InventoryCraftingWrapper;
import com.simon816.i15n.core.world.CustomWorld;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.CraftingManager;


public class TileAutoCrafting extends CustomTileEntity implements ITickable, InventoryProvider, SimpleObserver {

    private final List<ContainerAutoWorkbench> containers = Lists.newArrayList();

    private int percentageLeft = -100;
    ItemStack currentGoalOut;
    private boolean ready;

    private InventoryCrafting internalInv;

    private InventoryAdapter inventory;

    public TileAutoCrafting(CustomWorld world, Vector3i pos) {
        super(world, pos);
        SimpleSidedInventory sided = new SimpleSidedInventory(new SimpleInventory(9));
        sided.setPullHandler(new PullHandler() {

            @Override
            public ItemStack pull(Direction side) {
                if (canTakeOutput()) {
                    ItemStack out = TileAutoCrafting.this.currentGoalOut;
                    TileAutoCrafting.this.currentGoalOut = null;
                    onInventoryChange();
                    return out;
                }
                return null;
            }

            @Override
            public ItemStack peek(Direction side) {
                if (canTakeOutput()) {
                    return TileAutoCrafting.this.currentGoalOut;
                }
                return null;
            }
        }, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        sided.setPushHandler((stack, side) -> {
            for (int i = 0; i < this.inventory.getSize(); i++) {
                ItemStack invStack = this.inventory.getStack(i);
                if (invStack != null && invStack.getItem().equals(stack.getItem())) {
                    int newQty = invStack.getQuantity() + stack.getQuantity();
                    if (invStack.getMaxStackQuantity() >= newQty) {
                        invStack.setQuantity(newQty);
                        return true;
                    }
                }
            }
            return false;
        } , Direction.UP);
        this.inventory = new ObservableInventory(sided, this);
    }

    @Override
    public InventoryAdapter getInventory() {
        return this.inventory;
    }

    public InventoryCrafting getInternalInventory() {
        if (this.internalInv == null) {
            this.internalInv = new InventoryCraftingWrapper(this.inventory, 3, 3);
        }
        return this.internalInv;
    }

    public void addContainer(ContainerAutoWorkbench container) {
        this.containers.add(container);
    }

    public void removeContainer(ContainerAutoWorkbench container) {
        this.containers.remove(container);
    }

    public int getPercentageLeft() {
        return this.percentageLeft;
    }

    public ItemStack getLiveStack() {
        if (this.currentGoalOut != null) {
            ItemStack stack = this.currentGoalOut.copy();
            stack.setQuantity(this.percentageLeft);
            return stack;
        }
        return null;
    }

    public ItemStack getResult() {
        return this.currentGoalOut;
    }

    @Override
    public void tick() {
        if (Utils.ticksPassed(1)) {
            boolean shouldUpdate = !this.ready;
            if (!this.ready) {
                if (this.currentGoalOut != null) {
                    if ((this.percentageLeft += 1) > -1) {
                        this.percentageLeft = 0;
                        this.ready = true;
                        shouldUpdate = true;
                    }
                }
            }
            shouldUpdate = shouldUpdate && !this.inventory.isEmpty();
            if (!shouldUpdate) {
                return;
            }

            ItemStack currentOutput;
            if (this.ready) {
                currentOutput = this.currentGoalOut;
            } else {
                currentOutput = getLiveStack();
            }

            for (ContainerAutoWorkbench container : this.containers) {
                container.setOutput(currentOutput);
            }
        }
    }

    private boolean takingOutput;

    public void onPickup(boolean isPickup) {
        this.takingOutput = isPickup;
    }

    public boolean canTakeOutput() {
        return this.ready;
    }

    @Override
    public void onInventoryChange() {
        if (this.takingOutput) {
            this.currentGoalOut = findRecipe();
            this.percentageLeft = -100;
            this.ready = false;
            return;
        }
        ItemStack prevGoal = this.currentGoalOut;
        this.currentGoalOut = findRecipe();
        if (this.currentGoalOut == null || !this.currentGoalOut.equalTo(prevGoal)) {
            this.percentageLeft = -100;
            this.ready = false;
        }
        ItemStack output = getLiveStack();
        for (ContainerAutoWorkbench container : this.containers) {
            container.setOutput(output);
        }
    }

    private ItemStack findRecipe() {
        return ItemStackUtil.fromNative(CraftingManager.getInstance().findMatchingRecipe(getInternalInventory(),
                (net.minecraft.world.World) this.world.getWorld()));
    }

    @Override
    public void readFrom(DataView data) {
        Optional<DataView> opInventory = data.getView(of("ingredients"));
        if (opInventory.isPresent()) {
            this.inventory.readFrom(opInventory.get());
        }
        this.percentageLeft = data.getInt(of("percent")).orElse(-100);
        this.currentGoalOut = data.getSerializable(of("output"), ItemStack.class).orElse(null);
        this.ready = this.currentGoalOut != null && this.percentageLeft == 0;
    }

    @Override
    public void writeTo(DataView data) {
        if (!this.inventory.isEmpty()) {
            data.set(of("ingredients"), this.inventory);
        }
        data.set(of("percent"), this.percentageLeft);
        if (this.currentGoalOut != null) {
            data.set(of("output"), this.currentGoalOut);
        }
    }

}
