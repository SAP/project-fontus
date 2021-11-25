package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.AllowedPurpose;
import com.sap.fontus.gdpr.metadata.ExpiryDate;
import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.Vendor;

import java.util.Objects;
import java.util.Set;

public class SimpleAllowedPurpose implements AllowedPurpose {

    private ExpiryDate expiryDate;
    private Purpose purpose;
    private Set<Vendor> vendors;

    public SimpleAllowedPurpose() {
        this.expiryDate = new SimpleExpiryDate();
        this.purpose = new SimplePurpose();
        this.vendors = Set.of();
    }

    public SimpleAllowedPurpose(ExpiryDate expiryDate, Purpose purpose, Set<Vendor> vendors) {
        this.expiryDate = expiryDate;
        this.purpose = purpose;
        this.vendors = vendors;
    }

    @Override
    public ExpiryDate getExpiryDate() {
        return expiryDate;
    }

    @Override
    public Purpose getAllowedPurpose() {
        return purpose;
    }

    @Override
    public Set<Vendor> getAllowedVendors() {
        return vendors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleAllowedPurpose that = (SimpleAllowedPurpose) o;
        return Objects.equals(expiryDate, that.expiryDate) && purpose == that.purpose && Objects.equals(vendors, that.vendors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiryDate, purpose, vendors);
    }

    @Override
    public String toString() {
        return "SimpleAllowedPurpose{" +
                "expiryDate=" + expiryDate +
                ", purpose=" + purpose +
                ", vendors=" + vendors +
                '}';
    }
}
