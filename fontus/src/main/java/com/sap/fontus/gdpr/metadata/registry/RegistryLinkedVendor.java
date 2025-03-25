package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.gdpr.metadata.Vendor;
import com.sap.fontus.gdpr.metadata.VendorBase;

import java.io.Serializable;

public class RegistryLinkedVendor extends VendorBase  implements Serializable {

    private String name;

    private Vendor getRegisteredVendor() {
        return VendorRegistry.getInstance().getOrRegisterObject(this.name);
    }

    public RegistryLinkedVendor(String name) {
        this.name = name;
        this.getRegisteredVendor();
    }

    public RegistryLinkedVendor() {
        this.name = "Default";
        this.getRegisteredVendor();
    }

    public void setName(String name) {
        this.name = name;
        this.getRegisteredVendor();
    }

    @Override
    public int getId() {
        return this.getRegisteredVendor().getId();
    }

    @Override
    public String getName() {
        return this.getRegisteredVendor().getName();
    }

}
