package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.gdpr.metadata.Vendor;
import com.sap.fontus.gdpr.metadata.simple.SimpleVendor;
import com.sap.fontus.utils.GenericRegistry;

public class VendorRegistry extends GenericRegistry<Vendor> {

    private static VendorRegistry instance;
    private static boolean isPopulated = false;

    public synchronized void populateFromConfiguration(Configuration c) {
        if (!isPopulated) {
            for (com.sap.fontus.config.Vendor v : c.getVendors()) {
                this.getOrRegisterObject(v.getName());
            }
            isPopulated = true;
        }
    }

    @Override
    protected synchronized Vendor getNewObject(String name, int id) {
        return new SimpleVendor(id, name);
    }

    public static synchronized VendorRegistry getInstance() {
        if (instance == null) {
            instance = new VendorRegistry();
            if (Configuration.isInitialized()) {
                instance.populateFromConfiguration(Configuration.getConfiguration());
            }
        }
        return instance;
    }

}
