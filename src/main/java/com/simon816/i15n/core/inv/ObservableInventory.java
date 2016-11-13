package com.simon816.i15n.core.inv;

import org.spongepowered.api.item.inventory.ItemStack;

public class ObservableInventory extends InventoryDelegate {

    public interface InventoryObserver {

        void onChange(int index);

        void onRemove(int index);

        void onSet(int index);

        void onClear();

        default InventoryObserver chained(InventoryObserver other) {
            InventoryObserver primary = this;
            return new InventoryObserver() {

                @Override
                public void onRemove(int index) {
                    primary.onRemove(index);
                    other.onRemove(index);
                }

                @Override
                public void onSet(int index) {
                    primary.onSet(index);
                    other.onSet(index);
                }

                @Override
                public void onClear() {
                    primary.onClear();
                    other.onClear();
                }

                @Override
                public void onChange(int index) {
                    primary.onChange(index);
                    other.onChange(index);
                }
            };
        }

    }

    public interface SimpleObserver extends InventoryObserver {
        void onInventoryChange();

        default void onInventoryChange(int index) {
            onInventoryChange();
        }

        @Override
        default void onChange(int index) {
            onInventoryChange(index);
        }


        @Override
        default void onClear() {
            onInventoryChange();
        }

        @Override
        default void onSet(int index) {
            onInventoryChange(index);
        }

        @Override
        default void onRemove(int index) {
            onInventoryChange(index);
        }
    }

    private InventoryObserver observer;

    public ObservableInventory(InventoryAdapter inv, InventoryObserver observer) {
        super(inv);
        this.observer = observer;
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        super.setStack(index, stack);
        if (stack == null) {
            this.observer.onRemove(index);
        } else {
            this.observer.onSet(index);
        }
    }

    @Override
    public ItemStack removeStack(int index) {
        ItemStack stack = super.removeStack(index);
        this.observer.onRemove(index);
        return stack;
    }

    @Override
    public ItemStack decrementStack(int index, int count) {
        ItemStack stack = super.decrementStack(index, count);
        this.observer.onChange(index);
        return stack;
    }

    @Override
    public void clear() {
        super.clear();
        this.observer.onClear();
    }

}
