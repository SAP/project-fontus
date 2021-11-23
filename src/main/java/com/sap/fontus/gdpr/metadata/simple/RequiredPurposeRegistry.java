package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.config.Sink;
import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.RequiredPurposes;
import com.sap.fontus.gdpr.metadata.Vendor;

import java.util.Collection;
import java.util.HashSet;

public class RequiredPurposeRegistry {

    public static RequiredPurposes getPurposeFromSink(Sink sink) {
        PurposeRegistry pr = PurposeRegistry.getInstance();
        VendorRegistry vr = VendorRegistry.getInstance();

        Collection<Purpose> purposes = new HashSet<>();
        for (String purposeString : sink.getPurposes()) {
            Purpose p = pr.get(purposeString);
            if (p != null) {
                purposes.add(p);
            }
        }

        Collection<Vendor> vendors = new HashSet<>();
        for (String vendorString : sink.getVendors()) {
            Vendor v = vr.get(vendorString);
            if (v != null) {
                vendors.add(v);
            }
        }

        return new SimpleRequiredPurposes(purposes, vendors);
    }



}
