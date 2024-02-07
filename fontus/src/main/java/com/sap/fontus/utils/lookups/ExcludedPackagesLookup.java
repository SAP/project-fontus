package com.sap.fontus.utils.lookups;

import com.sap.fontus.config.Configuration;

public final class ExcludedPackagesLookup {
    private ExcludedPackagesLookup() {
    }

    public static boolean isExcludedPackage(String internalName) {
        for (String excludedPackage : Configuration.getConfiguration().getExcludedPackages()) {
            if(internalName.startsWith(excludedPackage)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExcludedClass(String internalName) {
        String clazzName = internalName;
        if(clazzName.contains("$")) {
            // TODO: is this sound? Trying to detect inner classes
            clazzName = clazzName.split("\\$")[0];
        }
        for (String excludedClass : Configuration.getConfiguration().getExcludedClasses()) {
            if(clazzName.equals(excludedClass)) {
                return true;
            }
        }
        return false;
    }
}
