package com.sap.fontus.gdpr.cookie;

import java.util.ArrayList;
import java.util.List;

public class Purpose {
    private String id;
    private List<Vendor> vendors;

    Purpose() {
        this.vendors = new ArrayList<>(1);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Vendor> getVendors() {
        return this.vendors;
    }

    public void setVendors(List<Vendor> vendors) {
        this.vendors = vendors;
    }

    @Override
    public String toString() {
        return String.format("Purpose{id='%s', vendors=%s}", this.id, this.vendors);
    }
}
