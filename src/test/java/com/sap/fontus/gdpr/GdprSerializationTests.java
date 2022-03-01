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

    @Test
    void testDeserialization() {
        String json = "{\n" +
                "    \"@class\": \"com.sap.fontus.taintaware.shared.IASTaintRanges\",\n" +
                "    \"length\": 20,\n" +
                "    \"ranges\": [\n" +
                "        {\n" +
                "            \"end\": 20,\n" +
                "            \"data\": {\n" +
                "                \"@class\": \"com.sap.fontus.gdpr.metadata.GdprTaintMetadata\",\n" +
                "                \"source\": {\n" +
                "                    \"id\": 1,\n" +
                "                    \"name\": \"getParameterValues\",\n" +
                "                    \"@class\": \"com.sap.fontus.taintaware.shared.IASTaintSource\"\n" +
                "                },\n" +
                "                \"metadata\": {\n" +
                "                    \"@class\": \"com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata\",\n" +
                "                    \"dataId\": {\n" +
                "                        \"uuid\": \"9e8d3ce0-db91-4402-a429-bfe0ef9ecd28\",\n" +
                "                        \"@class\": \"com.sap.fontus.gdpr.metadata.simple.SimpleDataId\"\n" +
                "                    },\n" +
                "                    \"dataSubject\": {\n" +
                "                        \"id\": \"7241732\",\n" +
                "                        \"@class\": \"com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject\"\n" +
                "                    },\n" +
                "                    \"portability\": true,\n" +
                "                    \"allowedPurposes\": [],\n" +
                "                    \"identifiability\": \"NotExplicit\",\n" +
                "                    \"protectionLevel\": \"Normal\",\n" +
                "                    \"processingUnrestricted\": true\n" +
                "                }\n" +
                "            },\n" +
                "            \"start\": 0,\n" +
                "            \"@class\": \"com.sap.fontus.taintaware.shared.IASTaintRange\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        IASString restored = IASString.fromString("foo");
        Utils.restoreTaint(restored, json);
    }
}
