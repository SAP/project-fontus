package com.sap.fontus.gdpr.petclinic;

import com.sap.fontus.gdpr.metadata.AllowedPurpose;
import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.Vendor;
import com.sap.fontus.gdpr.metadata.simple.PurposeRegistry;
import com.sap.fontus.gdpr.metadata.simple.SimpleAllowedPurpose;
import com.sap.fontus.gdpr.metadata.simple.VendorRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConsentCookieMetadata {

    public static Collection<AllowedPurpose> getAllowedPurposesFromConsentCookie(ConsentCookie cookie) {
        Collection<AllowedPurpose> purposes = new ArrayList<>();

        for (ConsentCookie.Purpose p : cookie.getPurposes()) {
            Purpose purpose = PurposeRegistry.getInstance().getOrRegisterObject(p.getId());
            Set<Vendor> vendors = new HashSet<>();
            for (ConsentCookie.Vendor v : p.getVendors()) {
                Vendor vendor = VendorRegistry.getInstance().getOrRegisterObject(v.getId());
                if (v.isChecked()) {
                    vendors.add(vendor);
                }
            }
            purposes.add(new SimpleAllowedPurpose(purpose, vendors));
        }
        return purposes;
    }

}
