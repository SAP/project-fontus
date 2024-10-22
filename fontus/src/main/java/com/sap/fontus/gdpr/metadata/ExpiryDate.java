package com.sap.fontus.gdpr.metadata;

import java.time.Instant;

public interface ExpiryDate extends Comparable<ExpiryDate> {

    Instant getDate();

    boolean hasExpiry();

}
