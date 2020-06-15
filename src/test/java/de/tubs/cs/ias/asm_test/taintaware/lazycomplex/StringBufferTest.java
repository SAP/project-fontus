package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringBufferTest extends AbstractTest {

    @Test
    public void testSubstring_3() {
        IASString str = new IASString("hello");
        IASStringBuffer sb = new IASStringBuffer(str);

        this.getTaintChecker().setTaint(sb, true);

        int a_start = (int) (Math.random() * sb.length());
        int b_start = (int) (Math.random() * sb.length());
        int a = 2;
        int b = 4;

        IASString s = sb.substring(a, b);

        assertEquals(str.substring(a, b).getString(), s.getString());
        assertEquals("hello", str.getString());
        assertFalse(this.getTaintChecker().getTaint(str));
        assertTrue(this.getTaintChecker().getTaint(sb));
        assertTrue(this.getTaintChecker().getTaint(s));
    }
}
