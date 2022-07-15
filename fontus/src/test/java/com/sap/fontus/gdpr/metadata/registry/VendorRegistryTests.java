package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.gdpr.metadata.Vendor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class VendorRegistryTests {

    @Test
    void testVendorRegistry() {
        Vendor p = new RegistryLinkedVendor("acme");
        Vendor p2 = VendorRegistry.getInstance().getOrRegisterObject("acme");

        assertEquals(p, p2);
    }

    @Test
    void testVendorEquals() {
        Vendor p = new RegistryLinkedVendor("acme");
        Vendor p2 = new RegistryLinkedVendor("acme");

        assertEquals(p, p2);
    }

    @Test
    void testPurposeNotEquals() {
        Vendor p = new RegistryLinkedVendor("acme");
        Vendor p2 = new RegistryLinkedVendor("sap");

        assertNotEquals(p, p2);
    }

}