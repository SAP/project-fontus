package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.*;

import java.time.Instant;
import java.util.Set;

public class SimplePurposePolicy implements PurposePolicy {

    @Override
    public boolean areRequiredPurposesAllowed(RequiredPurposes required, AllowedPurposes allowed) {
        for (RequiredPurpose r : required) {
            if (!isRequiredPurposeAllowed(r, allowed)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isRequiredPurposeAllowed(RequiredPurpose required, AllowedPurposes allowed) {
        boolean isAllowed = false;
        for (AllowedPurpose a : allowed) {
            // Check consent has not expired
            if (!hasExpired(a.getExpiryDate())) {
                // Check whether the required purpose is in the list of allowed purposes
                if (a.getAllowedPurpose().equals(required.getPurpose())) {
                    // Check whether the vendor is allowed
                    if (a.getAllowedVendors().contains(required.getVendor())) {
                        isAllowed = true;
                        break;
                    }
                }
            }
        }
        return isAllowed;
    }

    private boolean hasExpired(ExpiryDate date) {
        if (date.hasExpiry()) {
            if (date.getDate().isBefore(Instant.now())) {
                return true;
            }
        }
        return false;
    }

}
