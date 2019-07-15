package com.simon816.i15n.compat;


public interface CatalogKey extends Comparable<CatalogKey> {

    String getNamespace();

    String getValue();

}
