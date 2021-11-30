package com.sap.fontus.gdpr.consentcookie;
import com.sap.fontus.gdpr.petclinic.ConsentCookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConsentCookieTest {

    @Test
    void testParser1() {
        String cookie = "eyJwdXJwb3NlcyI6W3siaWQiOiJzdG9yYWdlIiwidmVuZG9ycyI6W3siaWQiOiJ0dWJzIiwibmFtZSI6IlRVQlMiLCJjaGVja2VkIjp0cnVlfSx7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6InByb2Nlc3NpbmciLCJ2ZW5kb3JzIjpbeyJpZCI6InR1YnMiLCJuYW1lIjoiVFVCUyIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6Im1hcmtldGluZyIsInZlbmRvcnMiOlt7ImlkIjoic2FwIiwibmFtZSI6IlNBUCBTRSIsImNoZWNrZWQiOmZhbHNlfSx7ImlkIjoiYWNtZSIsIm5hbWUiOiJBQ01FIiwiY2hlY2tlZCI6ZmFsc2V9XX0seyJpZCI6ImxvZ2dpbmciLCJ2ZW5kb3JzIjpbeyJpZCI6InNhcCIsIm5hbWUiOiJTQVAgU0UiLCJjaGVja2VkIjpmYWxzZX0seyJpZCI6ImFjbWUiLCJuYW1lIjoiQUNNRSIsImNoZWNrZWQiOmZhbHNlfV19XX0=";
        ConsentCookie cc = ConsentCookie.parse(cookie);
        Assertions.assertNotNull(cc);
        Assertions.assertEquals(4, cc.getPurposes().size());
    }
}
