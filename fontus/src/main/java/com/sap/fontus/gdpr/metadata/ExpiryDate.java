package com.sap.fontus.gdpr.metadata;

import java.time.Instant;

public interface ExpiryDate extends Comparable<ExpiryDate> {

    public Instant getDate();

    public boolean hasExpiry();

}
