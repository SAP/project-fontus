package com.sap.fontus.taintaware.unified;

import org.junit.jupiter.api.Test;

import java.security.CodeSource;
import java.security.ProtectionDomain;

import static org.junit.jupiter.api.Assertions.assertNull;

class IASProtectionDomainTest {
    @Test
    void testFontusCodeSourceIsNull() {
        ProtectionDomain protectionDomain = IASString.class.getProtectionDomain();

        CodeSource codeSource = IASProtectionDomain.getCodeSource(protectionDomain);

        assertNull(codeSource);
    }

    @Test
    void testSystemCodeSourceIsNull() {
        ProtectionDomain protectionDomain = String.class.getProtectionDomain();

        CodeSource codeSource = IASProtectionDomain.getCodeSource(protectionDomain);

        assertNull(codeSource);
    }
}
