package com.sap.fontus.gdpr.metadata;

import java.util.Collection;
import java.util.Set;

public interface PurposePolicy {

    boolean areRequiredPurposesAllowed(RequiredPurposes required, Collection<AllowedPurpose> allowed);

}
