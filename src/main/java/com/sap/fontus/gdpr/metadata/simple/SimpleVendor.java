package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.Vendor;

import java.util.Objects;

public class SimpleVendor implements Vendor {

    private int id;

    public SimpleVendor(int id) {
        this.id = id;
    }

    @Override
    public int getVendorId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleVendor that = (SimpleVendor) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SimpleVendor{" +
                "id=" + id +
                '}';
    }
}
