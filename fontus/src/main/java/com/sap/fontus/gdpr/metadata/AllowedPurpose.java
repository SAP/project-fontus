package com.sap.fontus.gdpr.metadata;

import java.util.Set;

public interface AllowedPurpose {

    ExpiryDate getExpiryDate();
    void setExpiryDate(ExpiryDate expiryDate);
    Purpose getAllowedPurpose();

    Set<Vendor> getAllowedVendors();

}
