package com.sap.fontus.taintaware.lazybasic;

import com.sap.fontus.AbstractTest;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringTest extends AbstractTest {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.LAZYBASIC);
    }

    @Test
    public void testSubSequence_1() {
        IASString s = new IASString("hello");

        this.getTaintChecker().setTaint(s, true);

//            int a_start = (int) (Math.random() * s.length());
//            int b_start = (int) (Math.random() * s.length());
//            int a = Math.min(a_start, b_start);
//            int b = Math.max(a_start, b_start);
        int a = 1;
        int b = 1;

        IASString t;
        IASStringBuffer sb = new IASStringBuffer();
        t = (IASString) s.subSequence(a, b);
        for (int j = a; j < b; j++) {
            sb.append(s.charAt(j));
        }

        assertEquals("hello", s.getString());
        assertEquals(sb.toString(), t.getString());
        assertTrue(this.getTaintChecker().getTaint(s));
        if (t.isEmpty()) {
            assertFalse(this.getTaintChecker().getTaint(t), "String was tainted! a: " + a + " b: " + b + " string: " + t);
        } else {
            assertTrue(this.getTaintChecker().getTaint(t), "String: '" + t + "' was not tainted");
        }
    }

    @Test
    public void testJoin_4() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("bye");
        IASString delimiter = new IASString("-");

        this.getTaintChecker().setTaint(delimiter, true);

        IASString s = IASString.join(delimiter, s1, s2);

        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(delimiter));
        assertTrue(this.getTaintChecker().getTaint(s));
    }
}
