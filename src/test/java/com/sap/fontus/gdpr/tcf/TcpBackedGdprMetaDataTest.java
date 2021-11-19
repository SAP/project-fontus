package com.sap.fontus.gdpr.tcf;

import com.iabtcf.decoder.TCString;
import com.sap.fontus.gdpr.metadata.GdprMetadata;
import com.sap.fontus.gdpr.metadata.Purpose;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TcpBackedGdprMetaDataTest {

    private static String tc1 = "BOEFEAyOEFEAyAHABDENAI4AAAB9vABAASA";
    private static String tc2 = "COvFyGBOvFyGBAbAAAENAPCAAOAAAAAAAAAAAEEUACCKAAA.IFoEUQQgAIQwgIwQABAEAAAAOIAACAIAAAAQAIAgEAACEAAAAAgAQBAAAAAAAGBAAgAAAAAAAFAAECAAAgAAQARAEQAAAAAJAAIAAgAAAYQEAAAQmAgBC3ZAYzUw";

    @Test
    public void testVendorLoading() {
        Purpose p = VendorList.GetPurposeFromTcfId(1);
        assertNotNull(p);
        assertEquals(1, p.getId());
        assertEquals("Store and/or access information on a device", p.getName());
    }

    @Test
    public void testTCString() {
        TCString consent = TCString.decode(tc1);
        GdprMetadata metadata = new TcfBackedGdprMetadata(consent);

        assertEquals(3, metadata.getAllowedPurposes().size());
    }

    @Test
    public void testTCString2() {
        TCString consent = TCString.decode(tc2);
        GdprMetadata metadata = new TcfBackedGdprMetadata(consent);

        assertEquals(3, metadata.getAllowedPurposes().size());
    }
}
