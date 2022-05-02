package com.sap.fontus.config.abort;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.range.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ThrowingAbortTests {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    public void throwAbortTest() {
        String o = "FakeSinkObject";
        String s = "This is sensitive information";

        IASTaintInformation i = new IASTaintInformation(s.length());
        i.setTaint(8, 17, new IASBasicMetadata(1));
        IASString t = new IASString("This is sensitive information", i);

        Abort a = new ThrowingAbort();

        Exception e = assertThrows(TaintViolationException.class, () -> {
            a.abort(t, o, "fakeFunction", "fake", IASTaintHandler.getCleanedStackTrace());
        });

        assertEquals("Taint Violation: String: \"This is sensitive information\" entered function: fakeFunction", e.getMessage());
    }

}
