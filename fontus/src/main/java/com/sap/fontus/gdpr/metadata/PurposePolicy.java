package com.sap.fontus.gdpr.metadata;

import java.util.Collection;

public interface PurposePolicy {

    boolean areRequiredPurposesAllowed(RequiredPurposes required, Collection<AllowedPurpose> allowed);

}
