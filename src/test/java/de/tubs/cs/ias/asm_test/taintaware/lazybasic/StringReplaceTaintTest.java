package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASLazyAware;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class StringReplaceTaintTest {
    private Tainter tainter = new Tainter();

    private static class Tainter {
        public void setTaint(IASLazyAware str, boolean taint) {
            str.setTaint(taint);
        }

        public boolean getTaint(IASLazyAware str) {
            return str.isTainted();
        }
    }

    protected Tainter getTaintChecker() {
        return tainter;
    }

    @Test
    public void testReplace_1() {
        IASString s1 = new IASString("hello");
        // Assumption is that the setTaint() method
        // taints *all* characters in the IASString
        this.getTaintChecker().setTaint(s1, true);

        IASString s2 = s1.replace('l', 'z');

        assertEquals("hezzo", s2.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
    }

    @Test
    public void testReplace_2() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, false);

        IASString s2 = s1.replace('l', 'z');

        assertEquals("hezzo", s2.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
    }

    @Test
    public void testReplace_3() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, true);

        for (char c : s1.toCharArray()) {
            s1 = s1.replace(c, c);
        }

        assertEquals("hello", s1.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
    }

    @Test
    public void testReplace_4() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, false);

        for (char c : s1.toCharArray()) {
            s1 = s1.replace(c, c);
        }

        assertEquals("hello", s1.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
    }

    @Test
    public void testReplaceCharsequence_1() {
        IASString s = new IASString("hello");
        IASString sequence1 = new IASString("lo");
        IASString sequence2 = new IASString("bye");

        this.getTaintChecker().setTaint(s, true);
        this.getTaintChecker().setTaint(sequence1, false);
        this.getTaintChecker().setTaint(sequence2, false);

        IASString t = s.replace(sequence1, sequence2);

        assertEquals("hello", s.getString());
        assertEquals("lo", sequence1.getString());
        assertEquals("bye", sequence2.getString());
        assertEquals("helbye", t.getString());
        assertTrue(this.getTaintChecker().getTaint(s));
        assertFalse(this.getTaintChecker().getTaint(sequence1));
        assertFalse(this.getTaintChecker().getTaint(sequence2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_2() {
        IASString s = new IASString("hello");
        IASString sequence1 = new IASString("lo");
        IASString sequence2 = new IASString("bye");

        this.getTaintChecker().setTaint(s, false);
        this.getTaintChecker().setTaint(sequence1, false);
        this.getTaintChecker().setTaint(sequence2, true);

        IASString t = s.replace(sequence1, sequence2);

        assertEquals("hello", s.getString());
        assertEquals("lo", sequence1.getString());
        assertEquals("bye", sequence2.getString());
        assertEquals("helbye", t.getString());
        assertFalse(this.getTaintChecker().getTaint(s));
        assertFalse(this.getTaintChecker().getTaint(sequence1));
        assertTrue(this.getTaintChecker().getTaint(sequence2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_3() {
        IASString s = new IASString("hello");
        IASString sequence1 = new IASString("lo");
        IASString sequence2 = new IASString("bye");

        this.getTaintChecker().setTaint(s, false);
        this.getTaintChecker().setTaint(sequence1, true);
        this.getTaintChecker().setTaint(sequence2, false);

        IASString t = s.replace(sequence1, sequence2);

        assertEquals("hello", s.getString());
        assertEquals("lo", sequence1.getString());
        assertEquals("bye", sequence2.getString());
        assertEquals("helbye", t.getString());
        assertFalse(this.getTaintChecker().getTaint(s));
        assertTrue(this.getTaintChecker().getTaint(sequence1));
        assertFalse(this.getTaintChecker().getTaint(sequence2));
        assertFalse(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_4() {
        IASString s = new IASString("hello");
        IASString sequence1 = new IASString("lo");
        IASString sequence2 = new IASString("bye");

        this.getTaintChecker().setTaint(s, false);
        this.getTaintChecker().setTaint(sequence1, false);
        this.getTaintChecker().setTaint(sequence2, false);

        IASString t = s.replace(sequence1, sequence2);

        assertEquals("hello", s.getString());
        assertEquals("lo", sequence1.getString());
        assertEquals("bye", sequence2.getString());
        assertEquals("helbye", t.getString());
        assertFalse(this.getTaintChecker().getTaint(s));
        assertFalse(this.getTaintChecker().getTaint(sequence1));
        assertFalse(this.getTaintChecker().getTaint(sequence2));
        assertFalse(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_5() {
        IASString s = new IASString("hello");
        IASString sequence1 = new IASString("lo");
        IASString sequence2 = new IASString("bye");

        this.getTaintChecker().setTaint(s, true);
        this.getTaintChecker().setTaint(sequence1, true);
        this.getTaintChecker().setTaint(sequence2, true);

        IASString t = s.replace(sequence1, sequence2);

        assertEquals("hello", s.getString());
        assertEquals("lo", sequence1.getString());
        assertEquals("bye", sequence2.getString());
        assertEquals("helbye", t.getString());
        assertTrue(this.getTaintChecker().getTaint(s));
        assertTrue(this.getTaintChecker().getTaint(sequence1));
        assertTrue(this.getTaintChecker().getTaint(sequence2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_6() {
        IASString s = new IASString("hello");
        IASString sequence1 = new IASString("lo");
        IASString sequence2 = new IASString("bye");

        this.getTaintChecker().setTaint(s, false);
        this.getTaintChecker().setTaint(sequence1, true);
        this.getTaintChecker().setTaint(sequence2, true);

        IASString t = s.replace(sequence1, sequence2);

        assertEquals("hello", s.getString());
        assertEquals("lo", sequence1.getString());
        assertEquals("bye", sequence2.getString());
        assertEquals("helbye", t.getString());
        assertFalse(this.getTaintChecker().getTaint(s));
        assertTrue(this.getTaintChecker().getTaint(sequence1));
        assertTrue(this.getTaintChecker().getTaint(sequence2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_7() {
        IASString s = new IASString("hello");
        IASString sequence1 = new IASString("lo");
        IASString sequence2 = new IASString("bye");

        this.getTaintChecker().setTaint(s, true);
        this.getTaintChecker().setTaint(sequence1, false);
        this.getTaintChecker().setTaint(sequence2, true);

        IASString t = s.replace(sequence1, sequence2);

        assertEquals("hello", s.getString());
        assertEquals("lo", sequence1.getString());
        assertEquals("bye", sequence2.getString());
        assertEquals("helbye", t.getString());
        assertTrue(this.getTaintChecker().getTaint(s));
        assertFalse(this.getTaintChecker().getTaint(sequence1));
        assertTrue(this.getTaintChecker().getTaint(sequence2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_8() {
        IASString s = new IASString("hello");
        IASString sequence1 = new IASString("lo");
        IASString sequence2 = new IASString("bye");

        this.getTaintChecker().setTaint(s, true);
        this.getTaintChecker().setTaint(sequence1, true);
        this.getTaintChecker().setTaint(sequence2, false);

        IASString t = s.replace(sequence1, sequence2);

        assertEquals("hello", s.getString());
        assertEquals("lo", sequence1.getString());
        assertEquals("bye", sequence2.getString());
        assertEquals("helbye", t.getString());
        assertTrue(this.getTaintChecker().getTaint(s));
        assertTrue(this.getTaintChecker().getTaint(sequence1));
        assertFalse(this.getTaintChecker().getTaint(sequence2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    // Assumption: empty IASStrings have always a negative taint
    public void testReplaceCharsequence_9() {
        IASString s = new IASString("hello");

        this.getTaintChecker().setTaint(s, true);

        IASString t = s.replace(s, new IASString());

        assertEquals("hello", s.getString());
        assertEquals("", t.getString());
        assertTrue(this.getTaintChecker().getTaint(s));
        assertFalse(this.getTaintChecker().getTaint(t));
    }

    @Test
    // Assumption: empty IASStrings have always a negative taint
    public void testReplaceCharsequence_10() {
        IASString s = new IASString("hello");

        this.getTaintChecker().setTaint(s, false);

        IASString t = s.replace(s, new IASString());

        assertEquals("hello", s.getString());
        assertEquals("", t.getString());
        assertFalse(this.getTaintChecker().getTaint(s));
        assertFalse(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_11() {
        IASString s1 = new IASString("hello x");
        IASString s2 = new IASString("this is a very long IASString to test if long IASStrings can cause a problem here");
        IASString t = s1.replace("x", s2);

        assertEquals("hello x", s1.getString());
        assertEquals("this is a very long IASString to test if long IASStrings can cause a problem here", s2.getString());
        assertEquals("hello this is a very long IASString to test if long IASStrings can cause a problem here", t.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_12() {
        IASString s1 = new IASString("hello x");
        IASString s2 = new IASString("this is a very long IASString to test if long IASStrings can cause a problem here");

        this.getTaintChecker().setTaint(s1, true);

        IASString t = s1.replace("x", s2);

        assertEquals("hello x", s1.getString());
        assertEquals("this is a very long IASString to test if long IASStrings can cause a problem here", s2.getString());
        assertEquals("hello this is a very long IASString to test if long IASStrings can cause a problem here", t.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_13() {
        IASString s1 = new IASString("hello x");
        IASString s2 = new IASString("this is a very long IASString to test if long IASStrings can cause a problem here");

        this.getTaintChecker().setTaint(s2, true);

        IASString t = s1.replace("x", s2);

        assertEquals("hello x", s1.getString());
        assertEquals("this is a very long IASString to test if long IASStrings can cause a problem here", s2.getString());
        assertEquals("hello this is a very long IASString to test if long IASStrings can cause a problem here", t.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceCharsequence_14() {
        IASString s1 = new IASString("hello x");
        IASString s2 = new IASString("this is a very long IASString to test if long IASStrings can cause a problem here");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);

        IASString t = s1.replace("x", s2);

        assertEquals("hello x", s1.getString());
        assertEquals("this is a very long IASString to test if long IASStrings can cause a problem here", s2.getString());
        assertEquals("hello this is a very long IASString to test if long IASStrings can cause a problem here", t.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(t));
    }

    @Test
    public void testReplaceAll_1() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, false);

        IASString s = s1.replaceAll(new IASString("l*"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_2() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, true);

        IASString s = s1.replaceAll(new IASString("l*"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_3() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);

        IASString s = s1.replaceAll(new IASString("l*"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_4() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, false);

        IASString s = s1.replaceAll(new IASString("l*"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_5() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, true);

        IASString s = s1.replaceAll(new IASString("l*"), new IASString("zz"));

        assertEquals("hello", s1.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_6() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, false);

        IASString s = s1.replaceAll(new IASString("l*"), new IASString("zz"));

        assertEquals("hello", s1.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    // This is a tricky one: if the regex is tainted,
    // an attacker could control which parts of the
    // IASString are replaced.
    // I think here the data flow is *not* tainted, but the
    // control flow is.
    public void testReplaceAll_7() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("l*");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, false);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.replaceAll(s3, s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("l*", s3.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_8() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("l*");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, true);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.replaceAll(s3, s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("l*", s3.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_9() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("l*");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, false);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.replaceAll(s3, s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("l*", s3.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_10() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("l*");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.replaceAll(s3, s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("l*", s3.getString());
        assertEquals("zzhzzezzzzozz", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_11() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, true);

        IASString s = s1.replaceAll(new IASString("xyz"), new IASString("abc"));

        assertEquals("hello", s1.getString());
        assertEquals("hello", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_12() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, false);

        IASString s = s1.replaceAll(new IASString("xyz"), new IASString("abc"));

        assertEquals("hello", s1.getString());
        assertEquals("hello", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    // Assumption: empty IASStrings have always a negative taint
    public void testReplaceAll_13() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, false);

        IASString s = s1.replaceAll(new IASString("hello"), new IASString());

        assertEquals("hello", s1.getString());
        assertEquals("", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    // Assumption: empty IASStrings have always a negative taint
    public void testReplaceAll_14() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, true);

        IASString s = s1.replaceAll(new IASString("hello"), new IASString());

        assertEquals("hello", s1.getString());
        assertEquals("", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_15() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, true);

        IASString s = s1.replaceAll(new IASString("xyz"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("hello", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_16() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, false);

        IASString s = s1.replaceAll(new IASString("xyz"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("hello", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_17() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, false);

        IASString s = s1.replaceAll(new IASString("xyz"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("hello", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceAll_18() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);

        IASString s = s1.replaceAll(new IASString("xyz"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("hello", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_1() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, false);
        this.getTaintChecker().setTaint(s3, false);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("ll", s3.getString());
        assertEquals("hezzllo", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_2() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, false);
        this.getTaintChecker().setTaint(s3, false);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("ll", s3.getString());
        assertEquals("hezzllo", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s3));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_3() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, true);
        this.getTaintChecker().setTaint(s3, false);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("ll", s3.getString());
        assertEquals("hezzllo", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_4() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, false);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("ll", s3.getString());
        assertEquals("hezzllo", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_5() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);
        this.getTaintChecker().setTaint(s3, false);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("ll", s3.getString());
        assertEquals("hezzllo", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_6() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, false);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("ll", s3.getString());
        assertEquals("hezzllo", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_7() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, true);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("ll", s3.getString());
        assertEquals("hezzllo", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_8() {
        IASString s1 = new IASString("hellllo");
        IASString s2 = new IASString("zz");
        IASString s3 = new IASString("ll");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);
        this.getTaintChecker().setTaint(s3, true);

        IASString s = s1.replaceFirst(s3, s2);

        assertEquals("hellllo", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("ll", s3.getString());
        assertEquals("hezzllo", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s3));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_9() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, true);

        IASString s = s1.replaceFirst(new IASString("xyz"), new IASString("abc"));

        assertEquals("hello", s1.getString());
        assertEquals("hello", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_10() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, false);

        IASString s = s1.replaceFirst(new IASString("xyz"), new IASString("abc"));

        assertEquals("hello", s1.getString());
        assertEquals("hello", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_11() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, false);

        IASString s = s1.replaceFirst(new IASString("xyz"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("hello", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_12() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, false);
        this.getTaintChecker().setTaint(s2, true);

        IASString s = s1.replaceFirst(new IASString("xyz"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("hello", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_13() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, false);

        IASString s = s1.replaceFirst(new IASString("xyz"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("hello", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    public void testReplaceFirst_14() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("zz");

        this.getTaintChecker().setTaint(s1, true);
        this.getTaintChecker().setTaint(s2, true);

        IASString s = s1.replaceFirst(new IASString("xyz"), s2);

        assertEquals("hello", s1.getString());
        assertEquals("zz", s2.getString());
        assertEquals("hello", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(s));
    }

    @Test
    // Assumption: empty IASStrings have always a negative taint
    public void testReplaceFirst_15() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, true);

        IASString s = s1.replaceFirst(new IASString("hello"), new IASString());

        assertEquals("hello", s1.getString());
        assertEquals("", s.getString());
        assertTrue(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s));
    }

    @Test
    // Assumption: empty IASStrings have always a negative taint
    public void testReplaceFirst_16() {
        IASString s1 = new IASString("hello");

        this.getTaintChecker().setTaint(s1, false);

        IASString s = s1.replaceFirst(new IASString("hello"), new IASString());

        assertEquals("hello", s1.getString());
        assertEquals("", s.getString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertFalse(this.getTaintChecker().getTaint(s));
    }
}