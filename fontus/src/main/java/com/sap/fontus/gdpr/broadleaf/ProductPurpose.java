package com.sap.fontus.gdpr.broadleaf;

import com.sap.fontus.gdpr.metadata.AllowedPurpose;

import java.util.HashSet;
import java.util.Set;


public class ProductPurpose {

    private static ProductPurpose INSTANCE;
    private final Set<AllowedPurpose> productPurposes;

    private ProductPurpose() {
        this.productPurposes = new HashSet<>();
    }

    public static ProductPurpose getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProductPurpose();
        }

        return INSTANCE;
    }

    public Set<AllowedPurpose> getPurposes() {
        return this.productPurposes;
    }

    public void addPurpose(AllowedPurpose productPurpose) {
        boolean exists = false;
        for (AllowedPurpose a : this.productPurposes) {
            if (a.getAllowedPurpose().getId() == productPurpose.getAllowedPurpose().getId()) {
                exists = true;
            }
        }
        if (!exists) {
            this.productPurposes.add(productPurpose);
        }
    }

}
