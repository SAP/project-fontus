package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.Vendor;
import com.sap.fontus.utils.GenericRegistry;

public class VendorRegistry extends GenericRegistry<Vendor> {

    private static VendorRegistry instance;

    @Override
    protected synchronized Vendor getNewObject(String name, int id) {
        return new SimpleVendor(id, name);
    }

    public static synchronized VendorRegistry getInstance() {
        if (instance == null) {
            instance = new VendorRegistry();
        }
        return instance;
    }

}
