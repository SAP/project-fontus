package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.range.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiAbortTest {

    @Test
    public void throwAbortTest() {
        String o = "FakeSinkObject";
        String s = "This is sensitive information";

        IASTaintInformation i = new IASTaintInformation(s.length());
        i.setTaint(8, 17, new IASBasicMetadata(1));
        IASString t = new IASString("This is sensitive information", i);

        List<Abort> l = new ArrayList<>();
        l.add(new CensoringAbort());
        l.add(new ThrowingAbort());
        Abort a = new MultiAbort(l);

        Exception e = assertThrows(TaintViolationException.class, () -> {
            a.abort(t, o, "fakeFunction", "fake", IASTaintHandler.getCleanedStackTrace());
        });

        assertEquals("Taint Violation: String: \"This is ********* information\" entered function: fakeFunction", e.getMessage());
    }

}
