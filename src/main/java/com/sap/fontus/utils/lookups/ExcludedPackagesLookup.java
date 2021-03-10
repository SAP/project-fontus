package com.sap.fontus.utils.lookups;

import com.sap.fontus.config.Configuration;

public class ExcludedPackagesLookup {
    public static boolean isExcluded(String internalName) {
        for (String excludedPackage : Configuration.getConfiguration().getExcludedPackages()) {
            if (internalName.startsWith(excludedPackage)) {
                return true;
            }
        }
        return false;
    }
}
