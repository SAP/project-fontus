package com.sap.fontus.taintaware.unified;

import org.junit.jupiter.api.Test;

import java.security.CodeSource;
import java.security.ProtectionDomain;

import static org.junit.jupiter.api.Assertions.assertNull;

public class IASProtectionDomainTest {
    @Test
    public void testFontusCodeSourceIsNull() {
        ProtectionDomain protectionDomain = IASString.class.getProtectionDomain();

        CodeSource codeSource = IASProtectionDomain.getCodeSource(protectionDomain);

        assertNull(codeSource);
    }

    @Test
    public void testSystemCodeSourceIsNull() {
        ProtectionDomain protectionDomain = String.class.getProtectionDomain();

        CodeSource codeSource = IASProtectionDomain.getCodeSource(protectionDomain);

        assertNull(codeSource);
    }
}
