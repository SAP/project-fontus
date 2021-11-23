package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.RequiredPurposes;
import com.sap.fontus.gdpr.metadata.Vendor;

import java.util.Collection;
import java.util.Objects;

public class SimpleRequiredPurposes implements RequiredPurposes {

    private Collection<Purpose> purposeCollection;

    private Collection<Vendor> vendorCollection;

    public SimpleRequiredPurposes(Collection<Purpose> purposeCollection, Collection<Vendor> vendorCollection) {
        this.purposeCollection = purposeCollection;
        this.vendorCollection = vendorCollection;
    }

    public SimpleRequiredPurposes() {
    }

    @Override
    public Collection<Purpose> getPurposes() {
        return purposeCollection;
    }

    @Override
    public Collection<Vendor> getVendors() {
        return vendorCollection;
    }

    public void addPurpose(Purpose purpose) {
        purposeCollection.add(purpose);
    }

    public void addVendor(Vendor vendor) {
        vendorCollection.add(vendor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRequiredPurposes that = (SimpleRequiredPurposes) o;
        return Objects.equals(purposeCollection, that.purposeCollection) && Objects.equals(vendorCollection, that.vendorCollection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purposeCollection, vendorCollection);
    }

    @Override
    public String toString() {
        return "SimpleRequiredPurposes{" +
                "purposeCollection=" + purposeCollection +
                ", vendorCollection=" + vendorCollection +
                '}';
    }
}
