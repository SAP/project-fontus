package com.sap.fontus.gdpr.metadata;

import java.util.Collection;
import java.util.Collections;

public interface RequiredPurposes {

    Collection<Purpose> getPurposes();

    Collection<Vendor> getVendors();

    class EmptyRequiredPurposes implements RequiredPurposes {
        @Override
        public Collection<Purpose> getPurposes() {
            return Collections.emptyList();
        }

        @Override
        public Collection<Vendor> getVendors() {
            return Collections.emptyList();
        }
    }

}
