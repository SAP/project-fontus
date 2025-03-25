package com.sap.fontus.gdpr.cookie;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
    private static final Cache<ConsentCookie,Collection<AllowedPurpose>> purposeCache = Caffeine.newBuilder().build();

    public static Collection<AllowedPurpose> getAllowedPurposesFromConsentCookie(ConsentCookie cookie) {
        return purposeCache.get(cookie, (ignored)-> {
            Collection<AllowedPurpose> purposes = new ArrayList<>(10);

            for (com.sap.fontus.gdpr.cookie.Purpose p : cookie.getPurposes()) {
                Purpose purpose = new RegistryLinkedPurpose(p.getId());
                Set<Vendor> vendors = new HashSet<>(5);
                for (com.sap.fontus.gdpr.cookie.Vendor v : p.getVendors()) {
                    Vendor vendor = new RegistryLinkedVendor(v.getId());
                    if (v.isChecked()) {
                        vendors.add(vendor);
                    }
                }
                purposes.add(new SimpleAllowedPurpose(purpose, vendors));
            }
            return purposes;
        });
    }

}
