package com.sap.fontus.gdpr.metadata.tcf;

import com.iab.gdpr.Purpose;
import com.iab.gdpr.consent.VendorConsent;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleAllowedPurpose;
import com.sap.fontus.gdpr.metadata.simple.SimpleExpiryDate;
import com.sap.fontus.gdpr.metadata.simple.SimpleVendor;

import java.util.HashSet;
import java.util.Set;

public class TcfBackedGdprMetadata implements GdprMetadata {

    VendorConsent vc;

    public TcfBackedGdprMetadata(VendorConsent vendorConsent) {
        this.vc = vendorConsent;
    }

    @Override
    public Set<AllowedPurpose> getAllowedPurposes() {
        Set<AllowedPurpose> purposes = new HashSet<>();

        // Convert Vendors
        Set<Vendor> vendors = new HashSet<>();
        for (int v: vc.getAllowedVendorIds()) {
            vendors.add(new SimpleVendor(v));
        }

        // Convert purposes
        for (Purpose p : vc.getAllowedPurposes()) {
            purposes.add(new SimpleAllowedPurpose(
                    new SimpleExpiryDate(),
                    PurposeMap.ConvertFromTcfPurpose(p),
                    vendors));
        }
        return purposes;
    }

    @Override
    public ProtectionLevel getProtectionLevel() {
        return ProtectionLevel.Undefined;
    }

    @Override
    public DataSubject getSubject() {
        return null;
    }

    @Override
    public DataId getId() {
        return null;
    }

    @Override
    public boolean isQualifiedForPortability() {
        return false;
    }

    @Override
    public boolean isConsentGiven() {
        return true;
    }

    @Override
    public Identifiability isIdentifiabible() {
        return Identifiability.Undefined;
    }
}
