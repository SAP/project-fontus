package de.tubs.cs.ias.asm_test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaintAwareStringBufferTests {

    @Test
    void emptyStringBufferIsUntainted() {
        IASStringBuffer sb = new IASStringBuffer();
        assertFalse(sb.isTainted(), "If we construct a taint-aware StringBuffer without setting a taint, it should not be tainted.");
    }


    @Test
    void stringBufferFromUntaintedString() {
        IASString s = new IASString("hello");
        IASStringBuffer sb = new IASStringBuffer(s);
        assertFalse(sb.isTainted(), "If we construct a taint-aware StringBuffer from an untainted String, it should not be tainted.");
    }


    @Test
    void stringBufferFromTaintedString() {
        IASString s = new IASString("hello", true);
        IASStringBuffer sb = new IASStringBuffer(s);
        assertTrue(sb.isTainted(), "If we construct a taint-aware StringBuffer from a tainted String, it should be tainted.");
    }

    @Test
    void appendUntaintedString() {
        IASString s = new IASString("hello");
        IASStringBuffer sb = new IASStringBuffer();
        sb.append(s);
        assertFalse(sb.isTainted(), "If we append an untainted String to an untainted StringBuffer, it should not be tainted.");
    }

    @Test
    void appendTaintedString() {
        IASString s = new IASString("hello", true);
        IASStringBuffer sb = new IASStringBuffer();
        sb.append(s);
        assertTrue(sb.isTainted(), "If we append a tainted String to an untainted StringBuffer, it should not be tainted.");
    }
}
