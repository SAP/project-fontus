package com.sap.fontus.taintaware.range;

import com.sap.fontus.taintaware.range.testHelper.TaintMatcher;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.sap.fontus.taintaware.range.testHelper.RangeChainer.range;
import static com.sap.fontus.taintaware.range.testHelper.TaintMatcher.taintEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class FormatterTests {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    public void format_1() {
        IASString s = new IASString("%s", false);
        IASString repl = new IASString("Hello World!", true);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("Hello World!", s1.toString());
        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 12, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
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
        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 6, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN).add(7, 14, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
    }


    @Test
    public void format_4() {
        IASString s = new IASString("Hello %s World!", true);
        IASString repl = new IASString("1", false);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("Hello 1 World!", s1.toString());
        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 6, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN).add(7, 14, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
    }

    @Test
    public void format_5() {
        IASString s = new IASString("Hello %s World!", false);
        IASString repl = new IASString("1", true);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("Hello 1 World!", s1.toString());
        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(6, 7, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
    }

    @Test
    public void format_6() {
        IASString s = new IASString("%h", false);
        IASString repl = new IASString("hello", true);

        IASFormatter f = new IASFormatter();
        f.format(s, repl);

        IASString s1 = f.toIASString();
        assertEquals("5e918d2", s1.toString());
        MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 7, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
    }
}
