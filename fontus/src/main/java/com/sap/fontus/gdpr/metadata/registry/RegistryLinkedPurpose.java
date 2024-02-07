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
        this.getRegisteredPurpose();
    }

    public RegistryLinkedPurpose() {
        this.name = "Default";
        this.getRegisteredPurpose();
    }

    public void setName(String name) {
        this.name = name;
        this.getRegisteredPurpose();
    }

    @Override
    public int getId() {
        return this.getRegisteredPurpose().getId();
    }

    @Override
    public String getName() {
        return this.getRegisteredPurpose().getName();
    }

    @Override
    public String getDescription() {
        return this.getRegisteredPurpose().getDescription();
    }

    @Override
    public String getLegalDescription() {
        return this.getRegisteredPurpose().getLegalDescription();
    }

}
