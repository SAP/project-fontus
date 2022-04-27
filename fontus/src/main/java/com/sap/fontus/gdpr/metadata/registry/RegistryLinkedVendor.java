package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.gdpr.metadata.Vendor;
import com.sap.fontus.gdpr.metadata.VendorBase;

public class RegistryLinkedVendor extends VendorBase {

    private String name;

    private Vendor getRegisteredVendor() {
        return VendorRegistry.getInstance().getOrRegisterObject(this.name);
    }

    public RegistryLinkedVendor(String name) {
        this.name = name;
        getRegisteredVendor();
    }

    public RegistryLinkedVendor() {
        this.name = "Default";
        getRegisteredVendor();
    }

    public void setName(String name) {
        this.name = name;
        getRegisteredVendor();
    }

    @Override
    public int getId() {
        return getRegisteredVendor().getId();
    }

    @Override
    public String getName() {
        return getRegisteredVendor().getName();
    }

}
