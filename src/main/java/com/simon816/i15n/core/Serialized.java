package com.simon816.i15n.core;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;

public interface Serialized extends DataSerializable {

    void writeTo(DataView data);

    void readFrom(DataView data);

    default DataQuery of(String path) {
        return DataQuery.of(path);
    }

    @Override
    default int getContentVersion() {
        return 1;
    }

    @Override
    default DataContainer toContainer() {
        DataContainer data = Utils.emptyData().getContainer();
        writeTo(data);
        return data;
    }

}
