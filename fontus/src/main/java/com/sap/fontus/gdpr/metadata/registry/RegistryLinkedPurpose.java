package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.PurposeBase;

public class RegistryLinkedPurpose extends PurposeBase {

    private String name;

    private Purpose getRegisteredPurpose() {
        return PurposeRegistry.getInstance().getOrRegisterObject(this.name);
    }

    public RegistryLinkedPurpose(String name) {
        this.name = name;
        getRegisteredPurpose();
    }

    public RegistryLinkedPurpose() {
        this.name = "Default";
        getRegisteredPurpose();
    }

    public void setName(String name) {
        this.name = name;
        getRegisteredPurpose();
    }

    @Override
    public int getId() {
        return getRegisteredPurpose().getId();
    }

    @Override
    public String getName() {
        return getRegisteredPurpose().getName();
    }

    @Override
    public String getDescription() {
        return getRegisteredPurpose().getDescription();
    }

    @Override
    public String getLegalDescription() {
        return getRegisteredPurpose().getLegalDescription();
    }

}
