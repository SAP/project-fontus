package com.sap.fontus.gdpr.consentcookie;

import com.sap.fontus.gdpr.petclinic.ConsentCookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class ConsentCookieTest {

    @Test
    void testParser1() {
        String cookie = "eyJwdXJwb3NlcyI6W3siaWQiOiJzdG9yYWdlIiwidmVuZG9ycyI6W3siaWQiOiJ0dWJzIiwibmFtZSI6IlRVQlMiLCJjaGVja2VkIjp0cnVlfSx7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6InByb2Nlc3NpbmciLCJ2ZW5kb3JzIjpbeyJpZCI6InR1YnMiLCJuYW1lIjoiVFVCUyIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6Im1hcmtldGluZyIsInZlbmRvcnMiOlt7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6ImxvZ2dpbmciLCJ2ZW5kb3JzIjpbeyJpZCI6InNhcCIsIm5hbWUiOiJTQVAgU0UiLCJjaGVja2VkIjpmYWxzZX0seyJpZCI6ImFjbWUiLCJuYW1lIjoiQUNNRSIsImNoZWNrZWQiOmZhbHNlfV19XX0=";
        ConsentCookie cc = ConsentCookie.parse(cookie);
        Assertions.assertNotNull(cc);
        Assertions.assertEquals(4, cc.getPurposes().size());
    }

    @Test
    void testParser2() {
        String cookie = "eyJjcmVhdGVkIjoxNjQ1NzE0MDE1LCJwdXJwb3NlcyI6W3siaWQiOiJzdG9yYWdlIiwidmVuZG9ycyI6W3siaWQiOiJzYXAiLCJuYW1lIjoiU0FQIFNFIiwiY2hlY2tlZCI6dHJ1ZX0seyJpZCI6ImFjbWUiLCJuYW1lIjoiQUNNRSIsImNoZWNrZWQiOmZhbHNlfV19LHsiaWQiOiJwdWJsaXNoaW5nIiwidmVuZG9ycyI6W3siaWQiOiJzYXAiLCJuYW1lIjoiU0FQIFNFIiwiY2hlY2tlZCI6dHJ1ZX0seyJpZCI6ImFjbWUiLCJuYW1lIjoiQUNNRSIsImNoZWNrZWQiOmZhbHNlfV19LHsiaWQiOiJwcm9jZXNzaW5nIiwidmVuZG9ycyI6W3siaWQiOiJzYXAiLCJuYW1lIjoiU0FQIFNFIiwiY2hlY2tlZCI6dHJ1ZX0seyJpZCI6ImFjbWUiLCJuYW1lIjoiQUNNRSIsImNoZWNrZWQiOmZhbHNlfV19LHsiaWQiOiJtYXJrZXRpbmciLCJ2ZW5kb3JzIjpbeyJpZCI6InNhcCIsIm5hbWUiOiJTQVAgU0UiLCJjaGVja2VkIjp0cnVlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6ImxvZ2dpbmciLCJ2ZW5kb3JzIjpbeyJpZCI6InNhcCIsIm5hbWUiOiJTQVAgU0UiLCJjaGVja2VkIjp0cnVlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6dHJ1ZX1dfV19";
        ConsentCookie cc = ConsentCookie.parse(cookie);
        Assertions.assertNotNull(cc);
        Assertions.assertEquals(cc.getCreated(), Instant.ofEpochSecond(1645714015L));
        Assertions.assertEquals(5, cc.getPurposes().size());
    }
}
