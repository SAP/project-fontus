package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringTaintTest {
    private Tainter tainter = new Tainter();

    private static class Tainter {
        public void setTaint(IASLazyComplexAware str, boolean taint) {
            str.setTaint(taint);
        }

        public boolean getTaint(IASLazyComplexAware str) {
            return str.isTainted();
        }
    }

    protected Tainter getTaintChecker() {
        return tainter;
    }

    @Test
    public void testSplit_6() {
        IASString s1 = new IASString("bye");
        IASString s2 = new IASString(",bye");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);

        IASString result = new IASString(s1);
        for (int i = 0; i < 1000; i++) {
            result = result.concat(s2);
        }
        IASString[] t = result.split(new IASString(","));

        assertEquals("bye", s1.getString());
        assertEquals(",bye", s2.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        for (int i = 0; i < 1000; i++) {
            assertEquals("bye", t[i].getString());
            assertTrue(this.getTaintChecker().getTaint(t[i]), "Assertion failed at index " + i);
        }
    }

    @Test
    public void testIsEmpty() {
        IASString s = new IASString("");
        s.setTaint(true);

        // Empty string shouldn't be taintable
        assertFalse(s.isTainted());
    }
}
