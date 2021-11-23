package com.sap.fontus.gdpr.tcf;

import com.iabtcf.decoder.TCString;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleAllowedPurpose;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleExpiryDate;
import com.sap.fontus.gdpr.metadata.simple.SimpleVendor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TcfBackedGdprMetadata extends GdprMetadataBase {

    TCString tcString;
    DataId dataId;

    public TcfBackedGdprMetadata(TCString tcString){
        this.tcString = tcString;
        this.dataId = new SimpleDataId();
    }

    @Override
    public Collection<AllowedPurpose> getAllowedPurposes() {
        Collection<AllowedPurpose> purposes = new HashSet<>();

        // Convert Vendors
        Set<Vendor> vendors = new HashSet<>();
        try {
            for (int v : tcString.getAllowedVendors()) {
                vendors.add(new SimpleVendor(v, "Acme"));
            }
        } catch (Exception e) {
            //
        }

        // Convert purposes
        for (int p : tcString.getPurposesConsent()) {
            purposes.add(new SimpleAllowedPurpose(
                    new SimpleExpiryDate(),
                    VendorList.GetPurposeFromTcfId(p),
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TcfBackedGdprMetadata that = (TcfBackedGdprMetadata) o;
        return Objects.equals(tcString, that.tcString) && Objects.equals(dataId, that.dataId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tcString, dataId);
    }

}
