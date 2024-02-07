package com.sap.fontus.taintaware.range;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.helper.TaintMatcher;
import com.sap.fontus.taintaware.unified.IASFormatter;
import com.sap.fontus.taintaware.unified.IASString;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.sap.fontus.taintaware.helper.RangeChainer.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FormatterTests {

    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void format_1() {
        IASString s = new IASString("%s", false);
        IASString repl = new IASString("Hello World!", true);

        try(IASFormatter f = new IASFormatter()) {
            f.format(s, repl);

            IASString s1 = f.toIASString();
            assertEquals("Hello World!", s1.toString());
            MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 12, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        }
    }

    @Test
    void format_2() {
        IASString s = new IASString("%s", true);
        IASString repl = new IASString("Hello World!", false);

        try(IASFormatter f = new IASFormatter()) {
            f.format(s, repl);

            IASString s1 = f.toIASString();
            assertEquals("Hello World!", s1.toString());
            assertFalse(s1.isTainted());
        }
    }

    @Test
    void format_3() {
        IASString s = new IASString("Hello %d World!", true);
        int repl = 1;

        try (IASFormatter f = new IASFormatter()) {
            f.format(s, repl);

            IASString s1 = f.toIASString();
            assertEquals("Hello 1 World!", s1.toString());
            MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 6, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN).add(7, 14, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
        }
    }


    @Test
    void format_4() {
        IASString s = new IASString("Hello %s World!", true);
        IASString repl = new IASString("1", false);

        try(IASFormatter f = new IASFormatter()) {
            f.format(s, repl);

            IASString s1 = f.toIASString();
            assertEquals("Hello 1 World!", s1.toString());
            MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 6, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN).add(7, 14, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)));
        }
    }

    @Test
    void format_5() {
        IASString s = new IASString("Hello %s World!", false);
        IASString repl = new IASString("1", true);

        try(IASFormatter f = new IASFormatter()) {
            f.format(s, repl);

            IASString s1 = f.toIASString();
            assertEquals("Hello 1 World!", s1.toString());
            MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(6, 7, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        }
    }

    @Test
    void format_6() {
        IASString s = new IASString("%h", false);
        IASString repl = new IASString("hello", true);

        try(IASFormatter f = new IASFormatter()) {
            f.format(s, repl);

            IASString s1 = f.toIASString();
            assertEquals("5e918d2", s1.toString());
            MatcherAssert.assertThat(s1, TaintMatcher.taintEquals(range(0, 7, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)));
        }
    }
}
