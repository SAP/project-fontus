package com.sap.fontus.taintaware.lazycomplex;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class TURLDecoderTest {
    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.LAZYCOMPLEX);
    }

    private static final String csString = StandardCharsets.UTF_8.toString();
    private static final IASString cs = new IASString(csString);

    @Test
    public void testDecodingUntainted1() throws UnsupportedEncodingException {
        IASString s = new IASString("hello");

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    public void testDecodingUntainted2() throws UnsupportedEncodingException {
        IASString s = new IASString("h.e-l*l_o+World");

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    public void testDecodingUntainted3() throws UnsupportedEncodingException {
        IASString s = new IASString("hell%C3%BC");

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    public void testDecodingUntainted4() throws UnsupportedEncodingException {
        IASString s = new IASString("hell%E0%A0%80");

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    public void testDecodingTainted1() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hel");
        IASString s2 = new IASString("lo");
        s2.setTaint(true);
        IASString s = s1.concat(s2);

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{new IASTaintRange(3, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)}, encode.getTaintRanges().toArray());
    }

    @Test
    public void testDecodingTainted2() throws UnsupportedEncodingException {
        IASString s1 = new IASString("h");
        IASString s2 = new IASString(".");
        s2.setTaint(true);
        IASString s3 = new IASString("e");
        IASString s4 = new IASString("-");
        s4.setTaint(true);
        IASString s5 = new IASString("l");
        IASString s6 = new IASString("*");
        s6.setTaint(true);
        IASString s7 = new IASString("l");
        IASString s8 = new IASString("_");
        s8.setTaint(true);
        IASString s9 = new IASString("o");
        IASString s10 = new IASString("+");
        s10.setTaint(true);
        IASString s11 = new IASString("World");
        s11.setTaint(true);
        IASString s = s1.concat(s2).concat(s3).concat(s4).concat(s5).concat(s6).concat(s7).concat(s8).concat(s9).concat(s10).concat(s11);

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertFalse(s1.isTainted());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(1, 2, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(3, 4, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 6, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 8, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(9, 15, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
        }, encode.getTaintRanges().toArray());
    }

    @Test
    public void testDecodingTainted3() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        IASString s2 = new IASString("%C3%BC");
        s2.setTaint(true);
        IASString s3 = new IASString("+world");
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(4, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintRanges().toArray());
    }

    @Test
    public void testDecodingTainted4() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        IASString s2 = new IASString("%E0%A0%80");
        s2.setTaint(true);
        IASString s3 = new IASString("+world");
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(4, 5, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintRanges().toArray());
    }

    @Test
    public void testDecodingTainted5() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        s1.setTaint(true);
        IASString s2 = new IASString("%E0%A0%80");
        IASString s3 = new IASString("+world");
        s3.setTaint(true);
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(0, 4, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 11, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintRanges().toArray());
    }

    @Test
    public void testDecodingTainted6() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        s1.setTaint(true);
        IASString s2 = new IASString("%E0%A0%80");
        IASString s3 = new IASString("%C3%BC");
        s3.setTaint(true);
        IASString s4 = new IASString("+world");
        IASString s = s1.concat(s2).concat(s3).concat(s4);

        IASString encode = TURLDecoder.decode(s, cs);

        assertEquals(URLDecoder.decode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(0, 4, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 6, IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintRanges().toArray());
    }
}
