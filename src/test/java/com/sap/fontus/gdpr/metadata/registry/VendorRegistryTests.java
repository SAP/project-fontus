package com.sap.fontus.gdpr.metadata.registry;

import com.sap.fontus.gdpr.metadata.Purpose;
import com.sap.fontus.gdpr.metadata.Vendor;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class VendorRegistryTests {

    @Test
    public void testVendorRegistry() {
        Vendor p = new RegistryLinkedVendor("acme");
        Vendor p2 = VendorRegistry.getInstance().getOrRegisterObject("acme");

        assertEquals(p, p2);
    }

    @Test
    public void testVendorEquals() {
        Vendor p = new RegistryLinkedVendor("acme");
        Vendor p2 = new RegistryLinkedVendor("acme");

        assertEquals(p, p2);
    }

    @Test
    public void testPurposeNotEquals() {
        Vendor p = new RegistryLinkedVendor("acme");
        Vendor p2 = new RegistryLinkedVendor("sap");

        assertNotEquals(p, p2);
    }

}