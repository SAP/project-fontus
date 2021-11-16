package com.sap.fontus.gdpr.metadata.tcf;

import com.iabtcf.decoder.TCString;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleAllowedPurpose;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleExpiryDate;
import com.sap.fontus.gdpr.metadata.simple.SimpleVendor;

import java.util.HashSet;
import java.util.Set;

public class TcfBackedGdprMetadata implements GdprMetadata {

    TCString tcString;
    DataId dataId;

    public TcfBackedGdprMetadata(TCString tcString){
        this.tcString = tcString;
        this.dataId = new SimpleDataId();
    }

    @Override
    public Set<AllowedPurpose> getAllowedPurposes() {
        Set<AllowedPurpose> purposes = new HashSet<>();

        // Convert Vendors
        Set<Vendor> vendors = new HashSet<>();
        for (int v: tcString.getAllowedVendors()) {
            vendors.add(new SimpleVendor(v));
        }

        // Convert purposes
        for (int p : tcString.getPurposesConsent()) {
            purposes.add(new SimpleAllowedPurpose(
                    new SimpleExpiryDate(),
                    VendorList.ConvertFromTcfId(p),
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
        return dataId;
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
