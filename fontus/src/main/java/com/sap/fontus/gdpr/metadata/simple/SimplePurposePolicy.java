package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.*;

import java.time.Instant;
import java.util.Collection;

public class SimplePurposePolicy implements PurposePolicy {

    @Override
    public boolean areRequiredPurposesAllowed(RequiredPurposes required, Collection<AllowedPurpose> allowed) {
        boolean isAllowed = true;
        // All of these purposes need to be fulfilled
        for (Purpose r : required.getPurposes()) {
            boolean thisPurposeAllowed = false;
            // This must appear in the allowed list
            for (AllowedPurpose a : allowed) {
                // Check consent has not expired
                if (!this.hasExpired(a.getExpiryDate())) {
                    // Check whether the required purpose is in the list of allowed purposes
                    if (a.getAllowedPurpose().equals(r)) {
                        // Check that we allow all vendors which are required
                        if (a.getAllowedVendors().containsAll(required.getVendors())) {
                            thisPurposeAllowed = true;
                            break;
                        }
                    }
                }
            }
            if (!thisPurposeAllowed) {
                isAllowed = false;
                break;
            }
         }
        return isAllowed;
    }

    private boolean hasExpired(ExpiryDate date) {
        if (date.hasExpiry()) {
            return date.getDate().isBefore(Instant.now());
        }
        return false;
    }

}
