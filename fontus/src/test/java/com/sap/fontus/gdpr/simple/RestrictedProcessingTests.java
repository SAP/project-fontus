package com.sap.fontus.gdpr.simple;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.metadata.GdprMetadata;
import com.sap.fontus.gdpr.metadata.GdprTaintMetadata;
import com.sap.fontus.gdpr.metadata.Identifiability;
import com.sap.fontus.gdpr.metadata.ProtectionLevel;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class RestrictedProcessingTests {

    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void testRestrictedProcessing() {
        IASString pre = IASString.fromString("[");
        IASString post = IASString.fromString("]");
        IASString payload = IASString.fromString("hello");
        GdprMetadata metadata = new SimpleGdprMetadata(
                new ArrayList<>(),
                ProtectionLevel.Normal,
                new SimpleDataSubject("foo"),
                new SimpleDataId(),
                true,
                true,
                Identifiability.NotExplicit);
        payload.setTaint(new GdprTaintMetadata(1, metadata));
        Utils.markContested(payload.getTaintInformation());
        IASString result = pre.concat(payload).concat(post);
        Pair<IASTaintAware,Boolean> data = Utils.censorContestedParts(result);
        Assertions.assertTrue(data.y);
        Assertions.assertEquals("[*****]", data.x.toIASString().getString());
    }
}
