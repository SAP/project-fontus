package com.sap.fontus.taintaware.unified;

import java.security.CodeSource;
import java.security.ProtectionDomain;

public class IASProtectionDomain {
    private static final ProtectionDomain fontusProtectionDomain = IASProtectionDomain.class.getProtectionDomain();

    public static CodeSource getCodeSource(ProtectionDomain protectionDomain) {
        if (isFontusProtectionDomain(protectionDomain)) {
            return null;
        }
        return protectionDomain.getCodeSource();
    }

    private static boolean isFontusProtectionDomain(ProtectionDomain protectionDomain) {
        return fontusProtectionDomain == protectionDomain;
    }
}
