package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.config.Sink;
import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.RequiredPurposes;
import com.sap.fontus.gdpr.metadata.Vendor;
import com.sap.fontus.gdpr.metadata.simple.SimpleRequiredPurposes;

import java.util.Collection;
import java.util.HashSet;

public class RequiredPurposeRegistry {

    public static RequiredPurposes getPurposeFromSink(Sink sink) {

        Collection<Purpose> purposes = new HashSet<>();
        for (String purposeString : sink.getDataProtection().getPurposes()) {
            Purpose p = new RegistryLinkedPurpose(purposeString);
            if (p != null) {
                purposes.add(p);
            }
        }

        Collection<Vendor> vendors = new HashSet<>();
        for (String vendorString : sink.getDataProtection().getVendors()) {
            Vendor v = new RegistryLinkedVendor(vendorString);
            if (v != null) {
                vendors.add(v);
            }
        }

        return new SimpleRequiredPurposes(purposes, vendors);
    }



}
