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
                "    \"length\": 6,\n" +
                "    \"ranges\": [\n" +
                "        {\n" +
                "            \"@class\": \"com.sap.fontus.taintaware.shared.IASTaintRange\",\n" +
                "            \"data\": {\n" +
                "                \"@class\": \"com.sap.fontus.gdpr.metadata.GdprTaintMetadata\",\n" +
                "                \"metadata\": {\n" +
                "                    \"@class\": \"com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata\",\n" +
                "                    \"allowedPurposes\": [],\n" +
                "                    \"dataId\": {\n" +
                "                        \"@class\": \"com.sap.fontus.gdpr.metadata.simple.SimpleDataId\",\n" +
                "                        \"uuid\": \"bdc62f0c-a973-420e-9535-d1ce71fb47b3\"\n" +
                "                    },\n" +
                "                    \"dataSubject\": {\n" +
                "                        \"@class\": \"com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject\",\n" +
                "                        \"id\": \"-1\"\n" +
                "                    },\n" +
                "                    \"identifiability\": \"NotExplicit\",\n" +
                "                    \"portability\": true,\n" +
                "                    \"processingUnrestricted\": true,\n" +
                "                    \"protectionLevel\": \"Normal\"\n" +
                "                },\n" +
                "                \"source\": {\n" +
                "                    \"@class\": \"com.sap.fontus.taintaware.shared.IASTaintSource\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"name\": \"getParameterValues\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"end\": 6,\n" +
                "            \"start\": 0\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        IASString restored = IASString.fromString("foobar");
        Utils.restoreTaint(restored, json);
    }
}
