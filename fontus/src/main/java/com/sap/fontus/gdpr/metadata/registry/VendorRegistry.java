package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.gdpr.metadata.Vendor;
import com.sap.fontus.gdpr.metadata.simple.SimpleVendor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VendorRegistry  {

    private static VendorRegistry instance;
    private static boolean isPopulated = false;
    private final Map<String, Vendor> vendors;
    private int counter = 0;

    public VendorRegistry() {
        this.vendors = new ConcurrentHashMap<>(32);
    }

    public Vendor getOrRegisterObject(String name) {
        return this.vendors.computeIfAbsent(name, (ignored) -> {
            this.counter++;
            return new SimpleVendor(this.counter, name);
        });
    }

    public Vendor get(String name) {
        return this.vendors.get(name);
    }

    public synchronized void populateFromConfiguration(Configuration c) {
        if (!isPopulated) {
            for (com.sap.fontus.config.Vendor v : c.getVendors()) {
                this.getOrRegisterObject(v.getName());
            }
            isPopulated = true;
        }
    }

    protected Vendor getNewObject(String name, int id) {
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
