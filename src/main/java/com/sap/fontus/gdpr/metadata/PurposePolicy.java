package com.sap.fontus.gdpr.metadata;

import java.util.Set;

public interface PurposePolicy {

    boolean areRequiredPurposesAllowed(RequiredPurposes required, AllowedPurposes allowed);

    boolean isRequiredPurposeAllowed(RequiredPurpose required, AllowedPurposes allowed);

}
