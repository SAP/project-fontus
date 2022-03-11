package com.sap.fontus.gdpr;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.gdpr.cookie.ConsentCookie;
import com.sap.fontus.gdpr.cookie.ConsentCookieMetadata;
import com.sap.fontus.taintaware.unified.IASString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class GdprMetadataTests {
    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void testMetadata() {
        String cookie = "eyJwdXJwb3NlcyI6W3siaWQiOiJzdG9yYWdlIiwidmVuZG9ycyI6W3siaWQiOiJ0dWJzIiwibmFtZSI6IlRVQlMiLCJjaGVja2VkIjp0cnVlfSx7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6InByb2Nlc3NpbmciLCJ2ZW5kb3JzIjpbeyJpZCI6InR1YnMiLCJuYW1lIjoiVFVCUyIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6Im1hcmtldGluZyIsInZlbmRvcnMiOlt7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6ImxvZ2dpbmciLCJ2ZW5kb3JzIjpbeyJpZCI6InNhcCIsIm5hbWUiOiJTQVAgU0UiLCJjaGVja2VkIjpmYWxzZX0seyJpZCI6ImFjbWUiLCJuYW1lIjoiQUNNRSIsImNoZWNrZWQiOmZhbHNlfV19XX0=";
        ConsentCookie cc = ConsentCookie.parse(cookie);
        Assertions.assertNotNull(cc);
        Assertions.assertEquals(4, cc.getPurposes().size());
        Collection<AllowedPurpose> allowedPurposes = ConsentCookieMetadata.getAllowedPurposesFromConsentCookie(cc);
        Assertions.assertEquals(4, allowedPurposes.size());
        GdprMetadata metadata = new SimpleGdprMetadata(
                allowedPurposes,
                ProtectionLevel.Normal,
                new SimpleDataSubject("test"),
                new SimpleDataId(),
                true,
                true,
                Identifiability.NotExplicit);
        IASString taintAware = new IASString("foobar");
        taintAware.setTaint(new GdprTaintMetadata(1, metadata));
        Assertions.assertTrue(taintAware.isTainted());
        boolean adjusted = Utils.updateExpiryDatesAndProtectionLevel(taintAware, 14L, ProtectionLevel.Normal);
        Assertions.assertTrue(adjusted);

    }

}
