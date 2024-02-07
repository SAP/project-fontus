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

    private final TCString tcString;
    private final DataId dataId;

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
            for (int v : this.tcString.getAllowedVendors()) {
                vendors.add(new SimpleVendor(v, "Acme"));
            }
        } catch (Exception e) {
            //
        }

        // Convert purposes
        for (int p : this.tcString.getPurposesConsent()) {
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
    public Collection<DataSubject> getSubjects() {
        return null;
    }

    @Override
    public DataId getId() {
        return this.dataId;
    }

    @Override
    public boolean isQualifiedForPortability() {
        return false;
    }

    @Override
    public boolean isProcessingUnrestricted() {
        return true;
    }

    @Override
    public Identifiability isIdentifiable() {
        return Identifiability.Undefined;
    }

    @Override
    public void setProtectionLevel(ProtectionLevel protectionLevel) {
        // NOP
    }

    @Override
    public void restrictProcessing() {
        // NOP
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TcfBackedGdprMetadata that = (TcfBackedGdprMetadata) o;
        return Objects.equals(this.tcString, that.tcString) && Objects.equals(this.dataId, that.dataId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tcString, this.dataId);
    }

}
