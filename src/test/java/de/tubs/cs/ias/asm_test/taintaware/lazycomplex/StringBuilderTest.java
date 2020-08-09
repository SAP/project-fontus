package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintMethod;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASLazyAware;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringBuilderTest {
    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.LAZYCOMPLEX);
    }

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
    public void testMixed_1() {
        IASString s1 = new IASString("hello");
        IASString s2 = new IASString("bye");
        IASStringBuilder sb1 = new IASStringBuilder(s1);

        this.getTaintChecker().setTaint(s2, true);

        IASStringBuilder sb2 = sb1.append(new IASString("hello").concat(s1)).insert(0, s2);
        IASStringBuilder sb3 = sb2.reverse();
        IASStringBuilder sb4 = sb3.delete(0, 5);
        IASStringBuilder sb5 = sb4.replace(5, 10, new IASString("hohoho"));

        assertEquals("ollehhohohoeyb", sb1.toString());
        assertEquals("ollehhohohoeyb", sb2.toString());
        assertEquals("ollehhohohoeyb", sb3.toString());
        assertEquals("ollehhohohoeyb", sb4.toString());
        assertEquals("ollehhohohoeyb", sb5.toString());
        assertFalse(this.getTaintChecker().getTaint(s1));
        assertTrue(this.getTaintChecker().getTaint(s2));
        assertTrue(this.getTaintChecker().getTaint(sb1));
        assertTrue(this.getTaintChecker().getTaint(sb2));
        assertTrue(this.getTaintChecker().getTaint(sb3));
        assertTrue(this.getTaintChecker().getTaint(sb4));
        assertTrue(this.getTaintChecker().getTaint(sb5));
    }

    @Test
    public void testInsertCharSequence_6() {
        IASStringBuilder sb = new IASStringBuilder("hello");
        IASStringBuilder sb1 = new IASStringBuilder("hi");
        IASStringBuilder sb2 = new IASStringBuilder("bye");

        this.getTaintChecker().setTaint(sb, true);
        this.getTaintChecker().setTaint(sb1, true);
        this.getTaintChecker().setTaint(sb2, false);

        IASStringBuilder sb3 = sb.insert(2, sb1).insert(3, sb2);

        assertEquals("hehbyeillo", sb.toString());
        assertEquals("hi", sb1.toString());
        assertEquals("bye", sb2.toString());
        assertEquals("hehbyeillo", sb3.toString());
        assertTrue(this.getTaintChecker().getTaint(sb));
        assertTrue(this.getTaintChecker().getTaint(sb1));
        assertFalse(this.getTaintChecker().getTaint(sb2));
        assertTrue(this.getTaintChecker().getTaint(sb3));
    }
}
