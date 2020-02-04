package de.tubs.cs.ias.asm_test.taintaware.range;

import org.junit.jupiter.api.Test;

import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.RangeChainer.range;
import static de.tubs.cs.ias.asm_test.taintaware.range.testHelper.TaintMatcher.taintEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FormatterTests {
    @Test
    public void format_1() {
        IASString s = new IASString("%s", false);
        IASString repl = new IASString("Hello World!", true);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("Hello World!", s1.toString());
        assertThat(s1, taintEquals(range(0, 12, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)));
    }

    @Test
    public void format_2() {
        IASString s = new IASString("%s", true);
        IASString repl = new IASString("Hello World!", false);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("Hello World!", s1.toString());
        assertFalse(s1.isTainted());
    }

    @Test
    public void format_3() {
        IASString s = new IASString("Hello %d World!", true);
        int repl = 1;

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("Hello 1 World!", s1.toString());
        assertThat(s1, taintEquals(range(0, 6, IASTaintSource.TS_CS_UNKNOWN_ORIGIN).add(7, 14, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)));
    }


    @Test
    public void format_4() {
        IASString s = new IASString("Hello %s World!", true);
        IASString repl = new IASString("1", false);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("Hello 1 World!", s1.toString());
        assertThat(s1, taintEquals(range(0, 6, IASTaintSource.TS_CS_UNKNOWN_ORIGIN).add(7, 14, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)));
    }

    @Test
    public void format_5() {
        IASString s = new IASString("Hello %s World!", false);
        IASString repl = new IASString("1", true);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("Hello 1 World!", s1.toString());
        assertThat(s1, taintEquals(range(6, 7, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)));
    }

    @Test
    public void format_6() {
        IASString s = new IASString("%h", false);
        IASString repl = new IASString("hello", true);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("5e918d2", s1.toString());
        assertThat(s1, taintEquals(range(0, 7, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)));
    }
}
