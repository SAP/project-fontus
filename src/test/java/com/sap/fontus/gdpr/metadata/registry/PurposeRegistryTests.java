package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.gdpr.metadata.Purpose;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PurposeRegistryTests {

    @Test
    public void testPurposeRegistry() {
        Purpose p = new RegistryLinkedPurpose("storage");
        Purpose p2 = PurposeRegistry.getInstance().getOrRegisterObject("storage");

        assertEquals(p, p2);
    }

    @Test
    public void testPurposeEquals() {
        Purpose p = new RegistryLinkedPurpose("storage");
        Purpose p2 = new RegistryLinkedPurpose("storage");

        assertEquals(p, p2);
    }

    @Test
    public void testPurposeNotEquals() {
        Purpose p = new RegistryLinkedPurpose("storage");
        Purpose p2 = new RegistryLinkedPurpose("baking");

        assertNotEquals(p, p2);
    }

}
