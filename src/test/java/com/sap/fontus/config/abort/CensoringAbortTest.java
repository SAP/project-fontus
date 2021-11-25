package com.sap.fontus.config.abort;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.range.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CensoringAbortTest {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    public void censoringAbortTest() {
        String o = "FakeSinkObject";
        String s = "This is sensitive information";

        IASTaintInformation i = new IASTaintInformation(s.length());
        i.setTaint(8, 17, new IASBasicMetadata(1));
        IASString t = new IASString("This is sensitive information", i);

        Abort a = new CensoringAbort();
        IASTaintAware ta = a.abort(t, o, "fakeFunction", "fake", IASTaintHandler.getCleanedStackTrace());

        assertEquals("This is ********* information", ta.toIASString().getString());
    }

    @Test
    public void censoringUntaintedAbortTest() {
        String o = "FakeSinkObject";
        String s = "This is sensitive information";

        IASTaintInformation i = new IASTaintInformation(s.length());
        IASString t = new IASString(s);

        Abort a = new CensoringAbort();
        IASTaintAware ta = a.abort(t, o, "fakeFunction", "fake", IASTaintHandler.getCleanedStackTrace());

        assertEquals("This is sensitive information", ta.toIASString().getString());
    }

    @Test
    public void censoringAbortTestTwoRanges() {
        String o = "FakeSinkObject";
        String s = "This is sensitive information";

        IASTaintInformation i = new IASTaintInformation(s.length());
        i.setTaint(8, 17, new IASBasicMetadata(1));
        i.setTaint(18, s.length(), new IASBasicMetadata(1));
        IASString t = new IASString("This is sensitive information", i);

        Abort a = new CensoringAbort();
        IASTaintAware ta = a.abort(t, o, "fakeFunction", "fake", IASTaintHandler.getCleanedStackTrace());

        assertEquals("This is ********* ***********", ta.toIASString().getString());
    }

}
