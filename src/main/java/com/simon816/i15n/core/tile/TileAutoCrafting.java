package com.simon816.i15n.core.tile;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.Direction;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.simon816.i15n.core.ITickable;
import com.simon816.i15n.core.ImplUtil;
import com.simon816.i15n.core.Industrialization;
import com.simon816.i15n.core.Utils;
import com.simon816.i15n.core.inv.InventoryAdapter;
import com.simon816.i15n.core.inv.InventoryProvider;
import com.simon816.i15n.core.inv.InventoryTracker;
import com.simon816.i15n.core.inv.ObservableInventory;
import com.simon816.i15n.core.inv.ObservableInventory.SimpleObserver;
import com.simon816.i15n.core.inv.SimpleInventory;
import com.simon816.i15n.core.inv.SimpleSidedInventory;
import com.simon816.i15n.core.inv.SimpleSidedInventory.PullHandler;
import com.simon816.i15n.core.recipe.RecipeUtil;
import com.simon816.i15n.core.world.CustomWorld;
import com.simon816.i15n.core.world.WorldManager;


public class TileAutoCrafting extends CustomTileEntity implements ITickable, InventoryProvider, SimpleObserver {

    private final List<Container> containers = Lists.newArrayList();

    private int percentageLeft = -100;
    private ItemStack currentGoalOut;
    private boolean ready;
    private boolean hasZeroStacks = true;

    private InventoryAdapter inventory;

    private int tickSpeed = 2;
    private int incrementSpeed = 5;

    public TileAutoCrafting(CustomWorld world, Vector3i pos) {
        super(world, pos);
        Inventory rawInventory = Inventory.builder().of(InventoryArchetypes.WORKBENCH)
                .build(Industrialization.instance());
        SimpleInventory realInv = new SimpleInventory(10);
        realInv.setApiInventory(rawInventory);
        SimpleSidedInventory sided = new SimpleSidedInventory(realInv);
        sided.setPullHandler(new PullHandler() {

            @Override
            public ItemStack pull(Direction side) {
                if (canTakeOutput()) {
                    return takeOutput();
                }
                return null;
            }

            @Override
            public ItemStack peek(Direction side) {
                if (canTakeOutput()) {
                    return getResult();
                }
                return null;
            }
        }, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        sided.setPushHandler((stack, side) -> {
            for (int i = 1; i < this.inventory.getSize(); i++) {
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
        }, Direction.UP);
        this.inventory = new ObservableInventory(sided, this);
        ImplUtil.wrapInventory(rawInventory, this.inventory);
    }

    @Override
    public InventoryAdapter getInventory() {
        return this.inventory;
    }

    public void addContainer(Container container) {
        InventoryTracker.startTracking(container)
                .on(ClickInventoryEvent.class,
                        slotFilter(slot -> slot.parent() == this.inventory.getAPIInventory(),
                                (slotTransaction, cursorTransaction) -> {
                                    int slotNumber = ImplUtil.slotNumber(slotTransaction.getSlot());
                                    if (slotNumber == 0) {
                                        handleOutputClick(slotTransaction, cursorTransaction);
                                    } else {
                                        handleGridClick(slotTransaction, cursorTransaction);
                                    }
                                }));
        this.containers.add(container);
    }

    private <T extends ClickInventoryEvent> EventListener<T> slotFilter(Predicate<Slot> filter,
            BiConsumer<SlotTransaction, Transaction<ItemStackSnapshot>> fn) {
        return event -> {
            Transaction<ItemStackSnapshot> cursor = event.getCursorTransaction();
            this.wasChange = false;
            this.ignoreChange = true;
            for (SlotTransaction tr : event.getTransactions()) {
                if (!tr.isValid() || !filter.test(tr.getSlot())) {
                    continue;
                }
                // Ignore the crap where output slot changes even when not clicking on it
                if (ImplUtil.slotNumber(tr.getSlot()) == 0) {
                    if (!tr.getOriginal().createStack().equalTo(cursor.getFinal().createStack())) {
                        System.out.println("ignore");
                        continue;
                    }
                }
                fn.accept(tr, cursor);
                // cursor = new Transaction<>(cursor.getCustom().orElse(cursor.getOriginal()),
                // cursor.getFinal());
            }
            if (this.wasChange) {
                onGridChange();
            }
            this.ignoreChange = false;
            this.wasChange = false;
            checkZero();
        };
    }

    private void handleGridClick(SlotTransaction slotTransaction, Transaction<ItemStackSnapshot> cursorTransaction) {
        ItemStackSnapshot slotOrig = slotTransaction.getOriginal();
        ItemStackSnapshot curOrig = cursorTransaction.getOriginal();
        if (slotOrig == ItemStackSnapshot.NONE) {
            System.out.println("place new item");
            // placing new item
            ItemStack zeroStack = slotTransaction.getFinal().createStack();
            zeroStack.setQuantity(0);
            slotTransaction.setCustom(zeroStack.createSnapshot());
            // Cancel cursor change
            cursorTransaction.setCustom(curOrig);
        } else {
            // Trying to pick up
            if (curOrig == ItemStackSnapshot.NONE) {
                System.out.println("pick up 0 stack");
                // If they try to pick up the 0 stack with nothing in cursor:
                if (slotOrig.getCount() == 0) {
                    cursorTransaction.setCustom(ItemStackSnapshot.NONE);
                } else {
                    System.out.println("Reset to 0");
                    // Trying to pickup all items in that slot
                    // Reset slot to 0
                    if (slotTransaction.getFinal() == ItemStackSnapshot.NONE) {
                        ItemStack zeroStack = slotOrig.createStack();
                        zeroStack.setQuantity(0);
                        slotTransaction.setCustom(zeroStack.createSnapshot());
                    }
                }
            } else {
                // Trying to place item on top of current item

                // If they attempt to switch-out the 0 stack with another type
                if (slotOrig.getCount() == 0 && slotOrig.getType() != curOrig.getType()) {
                    System.out.println("switch out 0 stack");
                    ItemStack zeroStack = curOrig.createStack();
                    zeroStack.setQuantity(0);
                    slotTransaction.setCustom(zeroStack.createSnapshot());
                    cursorTransaction.setCustom(curOrig);
                    // } else if (this.currentGoalOut == null) {
                    // // Cannot place item if no recipe
                    // System.out.println("no recipe");
                    // slotTransaction.setValid(false);
                    // cursorTransaction.setCustom(curOrig);
                }
            }
        }
        if (canTakeOutput()) {
            System.out.println("force update output");
            setOutput(getResult());
        }
    }

    private void handleOutputClick(SlotTransaction slotTransaction, Transaction<ItemStackSnapshot> cursorTransaction) {
        // Trying to put item in slot
        if (slotTransaction.getFinal() != ItemStackSnapshot.NONE) {
            if (slotTransaction.getOriginal() != cursorTransaction.getFinal()) {
                System.out.println("false positive");
                // False positive
                return;
            }
            System.out.println("put item in output");
            slotTransaction.setValid(false);
            // TODO because Sponge is broken - set custom to original
            cursorTransaction.setCustom(cursorTransaction.getOriginal());
            return;
        }
        if (!canTakeOutput()) {
            System.out.println("take output before ready");
            slotTransaction.setValid(false);
            cursorTransaction.setCustom(cursorTransaction.getOriginal());
            return;
        }
        System.out.println("take output");
        takeOutput();
    }

    ItemStack takeOutput() {
        ItemStack output = this.inventory.getStack(0);
        List<ItemStack> remaining = RecipeUtil.getRemainingItems(this.inventory, 1, 9, -1, this.world.getWorld());
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack existing = this.inventory.getStack(i + 1);
            ItemStack replacement = remaining.get(i);
            if (existing != null) {
                ItemStack old = this.inventory.decrementStack(i + 1, 1);
                existing = this.inventory.getStack(i + 1);
                if (existing == null) {
                    old.setQuantity(0);
                    this.inventory.setStack(i + 1, old);
                }
            }
            if (replacement != null) {
                if (existing == null) {
                    this.inventory.setStack(i + 1, replacement);
                } else if (isItemsEqual(replacement, existing)) {
                    replacement.setQuantity(replacement.getQuantity() + existing.getQuantity());
                    this.inventory.setStack(i + 1, replacement);
                } else {
                    dropItem(replacement);
                }
            }
        }
        this.percentageLeft = -100;
        this.ready = false;
        onGridChange();
        return output;
    }

    private void dropItem(ItemStack stack) {
        Entity item = this.world.getWorld().createEntity(EntityTypes.ITEM, getPosition().toDouble().add(0.5, 0.5, 0.5));
        item.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
        this.world.getWorld().spawnEntity(item, WorldManager.SPAWN_CAUSE);
    }

    private boolean isItemsEqual(ItemStack stackA, ItemStack stackB) {
        return ItemStackComparators.TYPE.compare(stackA, stackB) == 0
                && ItemStackComparators.PROPERTIES.compare(stackA, stackB) == 0
                && ItemStackComparators.ITEM_DATA.compare(stackA, stackB) == 0;
    }

    private void checkZero() {
        this.hasZeroStacks = false;
        for (int i = 1; i <= 9; i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (stack != null && stack.getQuantity() == 0) {
                this.hasZeroStacks = true;
                break;
            }
        }
    }

    public void removeContainer(Container container) {
        InventoryTracker.stopTracking(container);
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
        if (Utils.ticksPassed(this.tickSpeed)) {
            boolean shouldUpdate = !this.ready;
            if (!this.ready) {
                if (this.currentGoalOut != null && !this.hasZeroStacks) {
                    if ((this.percentageLeft += this.incrementSpeed) > -1) {
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
                // this.ready = false;
                // this.percentageLeft = -100;
                currentOutput = this.currentGoalOut;
            } else {
                currentOutput = getLiveStack();
            }
            setOutput(currentOutput);
        }
    }

    private Player containerToPlayer(Container container) {
        return ImplUtil.containerToPlayer(container);
    }

    private void setOutput(ItemStack stack) {
        // System.out.println("Output: " + stack);
        this.inventory.setStack(0, stack);
        for (Container container : this.containers) {
            ImplUtil.forceSlotUpdate(container, 0, stack);
        }
    }

    public boolean canTakeOutput() {
        return this.ready;
    }

    @Override
    public void onInventoryChange() {
        onInventoryChange(-1);
    }

    @Override
    public void onInventoryChange(int index) {
        if (index >= 1 && index <= 9) {
            onGridChange();
        }
    }

    private boolean wasChange;
    private boolean ignoreChange;

    private void onGridChange() {
        this.wasChange = true;
        if (this.ignoreChange) {
            return;
        }
        checkZero();
        System.out.println("take output : " + canTakeOutput() + " zero: " + this.hasZeroStacks);
        ItemStack prevGoal = this.currentGoalOut;
        ItemStack newGoal = findRecipe();
        boolean recipeStillMatches = newGoal != null && newGoal.equalTo(prevGoal);
        if (canTakeOutput() && recipeStillMatches && !this.hasZeroStacks) {
            System.out.println("wait");
            // There is stuff waiting to be taken, don't do anything
            return;
        }
        this.currentGoalOut = newGoal;
        if (this.hasZeroStacks || !recipeStillMatches) {
            this.percentageLeft = -100;
            System.out.println("reset");
            this.ready = false;
        }
        ItemStack output = getLiveStack();
        setOutput(output);
    }

    private ItemStack findRecipe() {
        // Thread.dumpStack();
        ItemStack findRecipe = RecipeUtil.findRecipe(this.inventory, 1, 9, -1, this.world.getWorld());
        System.out.println(findRecipe);
        return findRecipe;
    }

    @Override
    public void readFrom(DataView data) {
        Optional<DataView> opInventory = data.getView(of("inventory"));
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
            data.set(of("inventory"), this.inventory);
        }
        data.set(of("percent"), this.percentageLeft);
        if (this.currentGoalOut != null) {
            data.set(of("output"), this.currentGoalOut);
        }
    }

    public void destroy(Cause closeCause) {
        this.ready = false;
        for (Container container : Lists.newArrayList(this.containers)) {
            removeContainer(container);
            containerToPlayer(container).closeInventory(closeCause);
        }
        for (int i = 0; i < this.inventory.getSize(); i++) {
            ItemStack item = this.inventory.getStack(i);
            if (item != null && item.getQuantity() > 0) {
                dropItem(item);
            }
        }
        this.inventory.clear();
    }

}
