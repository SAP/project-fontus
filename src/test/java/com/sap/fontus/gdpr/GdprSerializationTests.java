package com.sap.fontus.gdpr;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.gdpr.metadata.GdprMetadata;
import com.sap.fontus.gdpr.metadata.GdprTaintMetadata;
import com.sap.fontus.gdpr.metadata.Identifiability;
import com.sap.fontus.gdpr.metadata.ProtectionLevel;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.sql.driver.Utils;
import com.sap.fontus.taintaware.unified.IASString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;

public class GdprSerializationTests {


    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    public void testSerialization() {
        GdprMetadata metadata = new SimpleGdprMetadata(
                Set.of(),
                ProtectionLevel.Normal,
                new SimpleDataSubject("test"),
                new SimpleDataId(),
                true,
                true,
                Identifiability.NotExplicit);
        IASString foo = IASString.fromString("foo");
        foo.setTaint(new GdprTaintMetadata(1, metadata));
        assertNotNull(foo.getTaintInformation().getTaint(0));
        String json = Utils.serializeTaints(foo);
        IASString restored = IASString.fromString("foo");
        Utils.restoreTaint(restored, json);
        String json2 = Utils.serializeTaints(restored);
        assertEquals(json, json2);

    }
}
