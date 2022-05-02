package com.sap.fontus.gdpr.broadleaf;

import com.sap.fontus.gdpr.metadata.AllowedPurpose;

import java.util.HashSet;
import java.util.Set;


public class ProductPurpose {

    private static ProductPurpose INSTANCE;
    private Set<AllowedPurpose> productPurposes;

    private ProductPurpose() {
        productPurposes = new HashSet<>();
    }

    public static ProductPurpose getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProductPurpose();
        }

        return INSTANCE;
    }

    public Set<AllowedPurpose> getPurposes() {
        return productPurposes;
    }

    public void addPurpose(AllowedPurpose productPurpose) {
        boolean exists = false;
        for (AllowedPurpose a : productPurposes) {
            if (a.getAllowedPurpose().getId() == productPurpose.getAllowedPurpose().getId()) {
                exists = true;
            }
        }
        if (!exists) {
            productPurposes.add(productPurpose);
        }
    }

}
