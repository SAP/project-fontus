package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.gdpr.metadata.Purpose;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


class PurposeRegistryTests {

    @Test
    void testPurposeRegistry() {
        Purpose p = new RegistryLinkedPurpose("storage");
        Purpose p2 = PurposeRegistry.getInstance().getOrRegisterObject("storage");

        assertEquals(p, p2);
    }

    @Test
    void testPurposeEquals() {
        Purpose p = new RegistryLinkedPurpose("storage");
        Purpose p2 = new RegistryLinkedPurpose("storage");

        assertEquals(p, p2);
    }

    @Test
    void testPurposeNotEquals() {
        Purpose p = new RegistryLinkedPurpose("storage");
        Purpose p2 = new RegistryLinkedPurpose("baking");

        assertNotEquals(p, p2);
    }

}
