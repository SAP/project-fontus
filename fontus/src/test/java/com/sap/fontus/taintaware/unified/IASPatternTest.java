package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IASPatternTest {

    @BeforeAll
    static void setup() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    private static final Pattern camelCase = Pattern.compile("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])");
    private static final Pattern comma = Pattern.compile(",");

    @Test
    void testCommaRegex() {
        String test = "hello,world";

        String[] ar = comma.split(test);
        assertEquals(2, ar.length);
        assertEquals("hello", ar[0]);
        assertEquals("world", ar[1]);
    }

    @Test
    void testCommaIASPattern() {
        IASPattern pattern = new IASPattern(comma);
        IASString test = new IASString("hello,world");

        IASString[] ar = pattern.split(test);
        assertEquals(2, ar.length);
        assertEquals("hello", ar[0].getString());
        assertEquals("world", ar[1].getString());
    }

    @Test
    void testCamelCaseRegex() {
        String test = "patientName";

        String[] ar = camelCase.split(test);
        assertEquals(2, ar.length);
        assertEquals("patient", ar[0]);
        assertEquals("Name", ar[1]);
    }

    @Test
    void testCamelCaseRegexIASPattern() {
        IASPattern iasCamelCase = new IASPattern(camelCase);
        IASString test = new IASString("patientName");

        IASString[] ar = iasCamelCase.split(test);
        assertEquals(2, ar.length);
        assertEquals("patient", ar[0].getString());
        assertEquals("Name", ar[1].getString());
    }


}
