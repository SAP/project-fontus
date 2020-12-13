package de.tubs.cs.ias.asm_test.utils.lookups;

import de.tubs.cs.ias.asm_test.config.Configuration;

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
