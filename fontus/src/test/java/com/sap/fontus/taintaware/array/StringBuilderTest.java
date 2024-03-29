package com.sap.fontus.taintaware.array;

import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;
import com.sap.fontus.taintaware.unified.IASStringBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringBuilderTest {
    private static final IASTaintMetadata TAINT = IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN;

    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.ARRAY);
    }

    @Test
    void testConstructor1() {
        IASString string = new IASString("Hello World");

        IASStringBuilder sb = new IASStringBuilder(string);

        assertTrue(sb.isUninitialized());
        assertFalse(sb.isTainted());
        assertEquals("Hello World", sb.toString());
    }

    @Test
    void testConstructor2() {
        IASString string = new IASString("Hello World", true);

        IASStringBuilder sb = new IASStringBuilder(string);

        assertTrue(sb.isInitialized());
        assertTrue(sb.isTainted());
        assertEquals("Hello World", sb.toString());
    }

    @Test
    void testAppend1() {
        IASStringBuilder sb = new IASStringBuilder();
        IASString toAppend = IASString.fromString("Hello World");

        sb.append(toAppend);

        assertEquals("Hello World", sb.toString());
        assertFalse(sb.isTainted());
    }

    @Test
    void testAppend2() {
        IASStringBuilder sb = new IASStringBuilder("Hello");
        IASString toAppend = IASString.fromString(" World");

        sb.append(toAppend);

        assertEquals("Hello World", sb.toString());
        assertFalse(sb.isTainted());
    }

    @Test
    void testAppend3() {
        IASStringBuilder sb = new IASStringBuilder("Hello");
        IASString toAppend = new IASString(" World", true);

        sb.append(toAppend);

        assertEquals("Hello World", sb.toString());
        assertArrayEquals(new IASTaintMetadata[]{null, null, null, null, null, TAINT, TAINT, TAINT, TAINT, TAINT, TAINT}, ((IASTaintInformation) sb.getTaintInformationInitialized()).getTaints());
    }

    @Test
    void testAppend4() {
        IASStringBuilder sb = new IASStringBuilder("Hello");
        sb.setTaint(true);
        IASString toAppend = new IASString(" World", true);

        sb.append(toAppend);

        assertEquals("Hello World", sb.toString());
        assertArrayEquals(new IASTaintMetadata[]{TAINT, TAINT, TAINT, TAINT, TAINT, TAINT, TAINT, TAINT, TAINT, TAINT, TAINT}, ((IASTaintInformation) sb.getTaintInformationInitialized()).getTaints());
    }

    @Test
    void testAppend5() {
        IASStringBuilder sb = new IASStringBuilder("Hello");
        sb.setTaint(true);
        IASString toAppend = new IASString(" World");

        sb.append(toAppend);

        assertEquals("Hello World", sb.toString());
        assertArrayEquals(new IASTaintMetadata[]{TAINT, TAINT, TAINT, TAINT, TAINT, null, null, null, null, null, null}, ((IASTaintInformation) sb.getTaintInformationInitialized()).getTaints());
    }

    @Test
    void testInsert() {
        IASString s2 = new IASString("World");
        IASString s1 = new IASString("Hello ");
        IASStringBuffer sb1 = new IASStringBuffer(s2);

        sb1.setTaint(true);

        IASStringBuffer sb2 = sb1.insert(0, s1);

        assertEquals("Hello World", sb1.toString());
        assertEquals("Hello World", sb2.toString());
        assertFalse(s1.isTainted());
        assertFalse(s2.isTainted());
        assertTrue(sb1.isTainted());
        assertTrue(sb2.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{null, null, null, null, null, null, TAINT, TAINT, TAINT, TAINT, TAINT}, ((IASTaintInformation) sb1.getTaintInformationInitialized()).getTaints());
        assertArrayEquals(new IASTaintMetadata[]{null, null, null, null, null, null, TAINT, TAINT, TAINT, TAINT, TAINT}, ((IASTaintInformation) sb2.getTaintInformationInitialized()).getTaints());
    }

    @Test
    void testReplace1() {
        IASStringBuffer sb1 = new IASStringBuffer("hello");
        IASString s = new IASString("HELLO");

        sb1.setTaint(true);
        s.setTaint(false);

        IASStringBuffer sb2 = sb1.replace(0, 2, s);

        assertEquals("HELLOllo", sb1.toString());
        assertEquals("HELLOllo", sb2.toString());
        assertFalse(s.isTainted());
        assertArrayEquals(new IASTaintMetadata[]{null, null, null, null, null, TAINT, TAINT, TAINT}, ((IASTaintInformation) sb1.getTaintInformationInitialized()).getTaints());
        assertArrayEquals(new IASTaintMetadata[]{null, null, null, null, null, TAINT, TAINT, TAINT}, ((IASTaintInformation) sb2.getTaintInformationInitialized()).getTaints());
    }
}
