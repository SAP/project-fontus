package com.sap.fontus.taintaware.lazycomplex;


import com.sap.fontus.AbstractTest;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringBufferTest extends AbstractTest {
    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.LAZYCOMPLEX);
    }

    @Test
    public void testSubstring_3() {
        IASString str = new IASString("hello");
        IASStringBuffer sb = new IASStringBuffer(str);

        this.getTaintChecker().setTaint(sb, true);

        int a = 2;
        int b = 4;

        IASString s = sb.substring(a, b);

        assertEquals(str.substring(a, b).getString(), s.getString());
        assertEquals("hello", str.getString());
        assertFalse(this.getTaintChecker().getTaint(str));
        assertTrue(this.getTaintChecker().getTaint(sb));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testMixed_4() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, true);

        IASString s2 = new IASString("bye");
        IASString s3 = new IASString("hohoho");
        IASStringBuffer sb1 = new IASStringBuffer(s1);
        IASString s4 = new IASString("hello").concat(s1);
        IASStringBuffer sb2 = sb1.append(s4);
        IASString s5 = sb1.toIASString();
        IASStringBuffer sb3 = sb2.insert(0, s2);
        IASString s6 = sb3.toIASString();
        IASStringBuffer sb4 = sb2.delete(0, 5);
        IASString s7 = sb4.toIASString();
        IASString s8 = sb4.substring(5);
        IASStringBuffer sb5 = sb4.reverse();
        IASStringBuffer sb6 = sb5.append(s8);
        IASStringBuffer sb7 = sb6.replace(5, 10, s3);

        assertEquals("ollehhohohoollllohello", sb1.toString());
        assertEquals("ollehhohohoollllohello", sb2.toString());
        assertEquals("ollehhohohoollllohello", sb4.toString());
        assertEquals("llohello", s8.toString());
        assertEquals("ollehhohohoollllohello", sb5.toString());
        assertEquals("ollehhohohoollllohello", sb6.toString());
        assertEquals("ollehhohohoollllohello", sb7.toString());

        assertTrue(s1.isTainted());
        assertFalse(s2.isTainted());
        assertFalse(s3.isTainted());
        assertTrue(s4.isTainted());
        assertTrue(s5.isTainted());
        assertTrue(s6.isTainted());
        assertTrue(s7.isTainted());
        assertTrue(s8.isTainted());

        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s8));
        assertTrue(this.getTaintChecker().getTaint(sb1));
        assertTrue(this.getTaintChecker().getTaint(sb2));
        assertTrue(this.getTaintChecker().getTaint(sb4));
        assertTrue(this.getTaintChecker().getTaint(sb5));
        assertTrue(this.getTaintChecker().getTaint(sb6));
        assertTrue(this.getTaintChecker().getTaint(sb7));
    }
}
