package com.sap.fontus.gdpr.metadata;

import java.time.Instant;
import java.util.Set;

public interface AllowedPurpose {

    public ExpiryDate getExpiryDate();

    public Purpose getAllowedPurpose();

    public Set<Vendor> getAllowedVendors();

}
