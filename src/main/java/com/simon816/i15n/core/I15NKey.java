package com.simon816.i15n.core;

import java.util.Objects;

import org.spongepowered.api.CatalogKey;

public class I15NKey implements CatalogKey {

    private final String namespace;
    private final String value;

    public I15NKey(String namespace, String value) {
        this.namespace = namespace;
        this.value = value;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public int compareTo(CatalogKey o) {
        int name = this.namespace.compareTo(o.getNamespace());
        return name != 0 ? name : this.value.compareTo(o.getValue());
    }

    @Override
    public String toString() {
        return this.namespace + ':' + this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CatalogKey)) {
            return false;
        }
        CatalogKey other = (CatalogKey) obj;
        return other.getNamespace().equals(this.namespace) && other.getValue().equals(this.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namespace, this.value);
    }
}
