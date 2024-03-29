package com.sap.fontus.taintaware.range;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.TURLEncoder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TURLEncoderTest {
    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
        cs = new IASString(csString);
    }

    private static final String csString = StandardCharsets.UTF_8.toString();
    private static IASString cs;

    @Test
    void testEncodingUntainted1() throws UnsupportedEncodingException {
        IASString s = new IASString("hello");

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    void testEncodingUntainted2() throws UnsupportedEncodingException {
        IASString s = new IASString("h.e-l*l_o World");

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    void testEncodingUntainted3() throws UnsupportedEncodingException {
        IASString s = new IASString("hellü");

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    void testEncodingUntainted4() throws UnsupportedEncodingException {
        IASString s = new IASString("hellࠀ");

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    void testEncodingTainted1() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hel");
        IASString s2 = new IASString("lo");
        s2.setTaint(true);
        IASString s = s1.concat(s2);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{new IASTaintRange(3, 5, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)}, encode.getTaintInformationInitialized().getTaintRanges(encode.length()).getTaintRanges().toArray(new IASTaintRange[0]));
    }

    @Test
    void testEncodingTainted2() throws UnsupportedEncodingException {
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
        IASString s10 = new IASString(" ");
        s10.setTaint(true);
        IASString s11 = new IASString("World");
        s11.setTaint(true);
        IASString s = s1.concat(s2).concat(s3).concat(s4).concat(s5).concat(s6).concat(s7).concat(s8).concat(s9).concat(s10).concat(s11);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(s1.isTainted());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(1, 2, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(3, 4, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 6, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 8, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(9, 15, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
        }, encode.getTaintInformationInitialized().getTaintRanges(encode.length()).getTaintRanges().toArray(new IASTaintRange[0]));
    }

    @Test
    void testEncodingTainted3() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        IASString s2 = new IASString("ü");
        s2.setTaint(true);
        IASString s3 = new IASString(" world");
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(4, 10, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintInformationInitialized().getTaintRanges(encode.length()).getTaintRanges().toArray(new IASTaintRange[0]));
    }

    @Test
    void testEncodingTainted4() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        IASString s2 = new IASString("ࠀ");
        s2.setTaint(true);
        IASString s3 = new IASString(" world");
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(4, 13, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintInformationInitialized().getTaintRanges(encode.length()).getTaintRanges().toArray(new IASTaintRange[0]));
    }

    @Test
    void testEncodingTainted5() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        s1.setTaint(true);
        IASString s2 = new IASString("ࠀ");
        IASString s3 = new IASString(" world");
        s3.setTaint(true);
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(0, 4, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(13, 19, IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintInformationInitialized().getTaintRanges(encode.length()).getTaintRanges().toArray(new IASTaintRange[0]));
    }
}
