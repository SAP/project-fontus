package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

public class SimplePurposePolicy implements PurposePolicy {

    @Override
    public boolean areRequiredPurposesAllowed(RequiredPurposes required, Collection<AllowedPurpose> allowed) {
        boolean isAllowed = false;
        for (Purpose r : required.getPurposes()) {
            for (AllowedPurpose a : allowed) {
                // Check consent has not expired
                if (!hasExpired(a.getExpiryDate())) {
                    // Check whether the required purpose is in the list of allowed purposes
                    if (a.getAllowedPurpose().equals(required)) {
                        // Check that we allow all vendors which are required
                        if (a.getAllowedVendors().containsAll(required.getVendors())) {
                            isAllowed = true;
                            break;
                        }
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
