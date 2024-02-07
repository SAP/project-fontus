package com.sap.fontus.gdpr.metadata;

import java.util.Set;

public interface AllowedPurpose {

    public ExpiryDate getExpiryDate();
    public void setExpiryDate(ExpiryDate expiryDate);
    public Purpose getAllowedPurpose();

    public Set<Vendor> getAllowedVendors();

}
