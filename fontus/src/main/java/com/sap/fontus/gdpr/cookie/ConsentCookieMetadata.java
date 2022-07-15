package com.sap.fontus.gdpr.cookie;

import com.sap.fontus.gdpr.metadata.AllowedPurpose;
import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.Vendor;
import com.sap.fontus.gdpr.metadata.registry.RegistryLinkedPurpose;
import com.sap.fontus.gdpr.metadata.registry.RegistryLinkedVendor;
import com.sap.fontus.gdpr.metadata.simple.SimpleAllowedPurpose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ConsentCookieMetadata {

    private ConsentCookieMetadata() {
    }

    public static Collection<AllowedPurpose> getAllowedPurposesFromConsentCookie(ConsentCookie cookie) {
        Collection<AllowedPurpose> purposes = new ArrayList<>();

        for (com.sap.fontus.gdpr.cookie.Purpose p : cookie.getPurposes()) {
            Purpose purpose = new RegistryLinkedPurpose(p.getId());
            Set<Vendor> vendors = new HashSet<>();
            for (com.sap.fontus.gdpr.cookie.Vendor v : p.getVendors()) {
                Vendor vendor = new RegistryLinkedVendor(v.getId());
                if (v.isChecked()) {
                    vendors.add(vendor);
                }
            }
            purposes.add(new SimpleAllowedPurpose(purpose, vendors));
        }
        return purposes;
    }

}
