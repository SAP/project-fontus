package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.VendorBase;

import java.io.Serializable;

public class SimpleVendor extends VendorBase implements Serializable {

    private int id;

    private String name;

    public SimpleVendor() {
        this.id = -1;
        this.name = "DEFAULT";
    }

    public SimpleVendor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }


}
