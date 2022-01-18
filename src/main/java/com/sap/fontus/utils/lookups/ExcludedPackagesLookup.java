package com.sap.fontus.utils.lookups;

import com.sap.fontus.config.Configuration;

public class ExcludedPackagesLookup {
    public static boolean isExcluded(String internalName) {
        boolean isInnerClass = internalName.contains("$");
        for (String excludedPackage : Configuration.getConfiguration().getExcludedPackages()) {
            if (isInnerClass && internalName.startsWith(excludedPackage)) {
                return true;
            } else if(!isInnerClass && internalName.equals(excludedPackage)) {
                return true;
            }
        }
        return false;
    }
}
