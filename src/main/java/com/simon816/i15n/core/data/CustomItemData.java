package com.simon816.i15n.core.data;

import java.util.Optional;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import com.simon816.i15n.core.Utils;

public class CustomItemData extends AbstractData<CustomItemData, CustomItemData.Immutable> {

    private DataView data;

    public CustomItemData(DataView data) {
        this.data = data;
        registerGettersAndSetters();
    }

    public DataView getData() {
        return this.data;
    }

    @Override
    protected void registerGettersAndSetters() {}

    @Override
    public int getContentVersion() {
        return 1;
    }

    // getOrCreate
    @Override
    public Optional<CustomItemData> fill(DataHolder dataHolder, MergeFunction overlap) {
        return Optional.of(this);
    }

    // Never called from Sponge
    @Override
    public Optional<CustomItemData> from(DataContainer container) {
        this.data = container;
        return Optional.of(this);
    }

    @Override
    public CustomItemData copy() {
        return new CustomItemData(this.data.copy());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(this.data);
    }

    @Override

    public DataContainer toContainer() {
        DataContainer container = this.data.getContainer();
        Utils.merge(container, super.toContainer());
        return container;
    }

    public static class Immutable extends AbstractImmutableData<Immutable, CustomItemData> {

        private final DataView data;

        public Immutable(DataView data) {
            this.data = data.copy();
            registerGetters();
        }

        @Override
        protected void registerGetters() {}

        @Override
        public int getContentVersion() {
            return 1;
        }

        @Override
        public CustomItemData asMutable() {
            return new CustomItemData(this.data.copy());
        }

        @Override
        public DataContainer toContainer() {
            return super.toContainer().set(DataQuery.of("data"), this.data.copy());
        }

    }

    public static class Builder extends AbstractDataBuilder<CustomItemData>
            implements DataManipulatorBuilder<CustomItemData, Immutable> {

        public Builder() {
            super(CustomItemData.class, 1);
        }

        @Override
        public Optional<CustomItemData> buildContent(DataView container) throws InvalidDataException {
            return Optional.of(new CustomItemData(container));
        }

        @Override
        public CustomItemData create() {
            return new CustomItemData(Utils.emptyData());
        }

        // Never called from Sponge
        @Override
        public Optional<CustomItemData> createFrom(DataHolder dataHolder) {
            return Optional.empty();
        }

    }

}
