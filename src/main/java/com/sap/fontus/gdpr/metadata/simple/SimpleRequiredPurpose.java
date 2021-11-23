package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.RequiredPurpose;
import com.sap.fontus.gdpr.metadata.Vendor;

import java.util.Objects;

public class SimpleRequiredPurpose implements RequiredPurpose {

    private Purpose purpose;
    private Vendor vendor;

    public SimpleRequiredPurpose(Purpose purpose, Vendor vendor) {
        this.purpose = purpose;
        this.vendor = vendor;
    }

    @Override
    public Purpose getPurpose() {
        return purpose;
    }

    @Override
    public Vendor getVendor() {
        return vendor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRequiredPurpose that = (SimpleRequiredPurpose) o;
        return Objects.equals(purpose, that.purpose) && Objects.equals(vendor, that.vendor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purpose, vendor);
    }

    @Override
    public String toString() {
        return "SimpleRequiredPurpose{" +
                "purpose=" + purpose +
                ", vendor=" + vendor +
                '}';
    }
}
