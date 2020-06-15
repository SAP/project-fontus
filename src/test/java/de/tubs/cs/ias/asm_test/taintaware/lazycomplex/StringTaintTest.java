package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;


import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
            assertTrue("Assertion failed at index " + i, this.getTaintChecker().getTaint(t[i]));
        }
    }

    @Test
    public void testConcat_1() {
        IASString s1 = new IASString("hello", true);

        IASString s = s1.concat(s1);

        assertTrue(s.isTainted());
        assertEquals(1, s.getTaintRanges().size());
        assertEquals(0, s.getTaintRanges().get(0).getStart());
        assertEquals(s.length(), s.getTaintRanges().get(0).getEnd());
    }

    @Test
    public void testSplitWithLimit_5() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString(",hi");
        IASString s3 = new IASString(",bye");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, false);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.concat(s2).concat(s3);

        IASString[] t = s.split(new IASString(","), 2);

        assertEquals("hello", s1.getString());
        assertEquals(",hi", s2.getString());
        assertEquals(",bye", s3.getString());
        assertEquals("hello,hi,bye", s.getString());
        assertEquals("hello", t[0].getString());
        assertEquals("hi,bye", t[1].getString());
        assertTrue(this.getTaintChecker().getTaint(s));
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertFalse(this.getTaintChecker().getTaint(t[0]));
        assertTrue(this.getTaintChecker().getTaint(t[1]));
    }

    @Test
    public void testFormatWithLocale_2() {
        IASString s = new IASString("hello");

        this.getTaintChecker().setTaint(s, true);

        for (Locale locale : Locale.getAvailableLocales()) {
            IASString t = IASString.format(locale, new IASString("%s"), s);

            assertEquals("hello", t.getString());
            assertTrue(this.getTaintChecker().getTaint(t));
        }
        assertEquals("hello", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testConcat_2() {
        IASString s1 = new IASString("hello", true);

        IASString s = s1.concat(s1).concat(s1);

        assertTrue(s.isTainted());
        assertEquals(1, s.getTaintRanges().size());
        assertEquals(0, s.getTaintRanges().get(0).getStart());
        assertEquals(s.length(), s.getTaintRanges().get(0).getEnd());
    }

    @Test
    public void testConcat_3() {
        IASString s1 = new IASString("hello", true);

        IASString s = new IASString(s1);
        for (int i = 0; i < 1000 - 1; i++) {
            s = s.concat(s1);
        }

        assertTrue(s.isTainted());
        assertEquals(1, s.getTaintRanges().size());
        assertEquals(0, s.getTaintRanges().get(0).getStart());
        assertEquals(s.length(), s.getTaintRanges().get(0).getEnd());
    }

    @Test
    public void testSplitWithLimit() {
        IASString s = new IASString("hello,hello,hello,hello,hello,hello,hello,hello,hello,hello,hello,hello,hello,hello,hello", true);
        IASString[] ss = s.split(new IASString(","), 10);
        for (IASString s1 : ss) {
            assertTrue(s1.isTainted());
        }
    }

    @Test
    public void testSplitWithLimit_2() {
        IASString s1 = new IASString("bye");
        IASString s2 = new IASString(",bye");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);

        IASString result = new IASString(s1);
        for (int i = 0; i < 1000; i++) {
            result = result.concat(s2);
        }

        IASString[] t = result.split(new IASString(","), 500);

        for (int i = 0; i < t.length - 1; i++) {
            assertEquals("bye", t[i].getString());
            assertTrue(i + "-th iteration", this.getTaintChecker().getTaint(t[i]));
        }
        assertEquals("bye", s1.getString());
        assertEquals(",bye", s2.getString());
        assertTrue(this.getTaintChecker().getTaint(t[t.length - 2]));
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
    }

    @Test
    public void testSplitWithLimit_3() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString(",hi");
        IASString s3 = new IASString(",bye");
        IASString s4 = new IASString(",bye!");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, true);
        this.getTaintChecker().setTaint(s3, false);
        this.getTaintChecker().setTaint(s4, true);

        IASString s = s1.concat(s2).concat(s3).concat(s4);

        IASString[] t = s.split(new IASString(","), -1);

        assertEquals("hello", s1.getString());
        assertEquals(",hi", s2.getString());
        assertEquals(",bye", s3.getString());
        assertEquals(",bye!", s4.getString());
        assertEquals("hello,hi,bye,bye!", s.getString());
        assertEquals("hello", t[0].getString());
        assertEquals("hi", t[1].getString());
        assertEquals("bye", t[2].getString());
        assertEquals("bye!", t[3].getString());
        assertTrue(this.getTaintChecker().getTaint(s));
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s4));
        assertFalse(this.getTaintChecker().getTaint(t[0]));
        assertTrue(this.getTaintChecker().getTaint(t[1]));
        assertFalse(this.getTaintChecker().getTaint(t[2]));
        assertTrue(this.getTaintChecker().getTaint(t[3]));
    }

    @Test
    public void testIsEmpty() {
        IASString s = new IASString("");
        s.setTaint(true);

        // Empty string shouldn't be taintable
        assertFalse(s.isTainted());
    }
}
