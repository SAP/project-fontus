package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.AllowedPurpose;
import com.sap.fontus.gdpr.metadata.ExpiryDate;
import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.Vendor;

import java.util.Objects;
import java.util.Set;

public class SimpleAllowedPurpose implements AllowedPurpose {

    private ExpiryDate expiryDate;
    private final Purpose purpose;
    private final Set<Vendor> vendors;

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

    public SimpleAllowedPurpose(Purpose purpose, Set<Vendor> vendors) {
        this.expiryDate = new SimpleExpiryDate();
        this.purpose = purpose;
        this.vendors = vendors;
    }

    @Override
    public ExpiryDate getExpiryDate() {
        return this.expiryDate;
    }

    @Override
    public void setExpiryDate(ExpiryDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public Purpose getAllowedPurpose() {
        return this.purpose;
    }

    @Override
    public Set<Vendor> getAllowedVendors() {
        return this.vendors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SimpleAllowedPurpose that = (SimpleAllowedPurpose) o;
        return Objects.equals(this.expiryDate, that.expiryDate) && this.purpose == that.purpose && Objects.equals(this.vendors, that.vendors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.expiryDate, this.purpose, this.vendors);
    }

    @Override
    public String toString() {
        return "SimpleAllowedPurpose{" +
                "expiryDate=" + this.expiryDate +
                ", purpose=" + this.purpose +
                ", vendors=" + this.vendors +
                '}';
    }
}
