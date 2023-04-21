package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.RequiredPurposes;
import com.sap.fontus.gdpr.metadata.Vendor;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class SimpleRequiredPurposes implements RequiredPurposes, Serializable {

    private final Collection<Purpose> purposeCollection;

    private final Collection<Vendor> vendorCollection;

    public SimpleRequiredPurposes(Collection<Purpose> purposeCollection, Collection<Vendor> vendorCollection) {
        this.purposeCollection = purposeCollection;
        this.vendorCollection = vendorCollection;
    }

    public SimpleRequiredPurposes() {
        this.purposeCollection = new HashSet<>();
        this.vendorCollection = new HashSet<>();
    }

    @Override
    public Collection<Purpose> getPurposes() {
        return this.purposeCollection;
    }

    @Override
    public Collection<Vendor> getVendors() {
        return this.vendorCollection;
    }

    public void addPurpose(Purpose purpose) {
        this.purposeCollection.add(purpose);
    }

    public void addVendor(Vendor vendor) {
        this.vendorCollection.add(vendor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SimpleRequiredPurposes that = (SimpleRequiredPurposes) o;
        return Objects.equals(this.purposeCollection, that.purposeCollection) && Objects.equals(this.vendorCollection, that.vendorCollection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.purposeCollection, this.vendorCollection);
    }

    @Override
    public String toString() {
        return "SimpleRequiredPurposes{" +
                "purposeCollection=" + this.purposeCollection +
                ", vendorCollection=" + this.vendorCollection +
                '}';
    }
}
