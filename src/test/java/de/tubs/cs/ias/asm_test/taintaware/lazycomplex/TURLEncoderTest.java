package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

class TURLEncoderTest {
    private static final String csString = StandardCharsets.UTF_8.toString();
    private static final IASString cs = new IASString(csString);

    @Test
    public void testEncodingUntainted1() throws UnsupportedEncodingException {
        IASString s = new IASString("hello");

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    public void testEncodingUntainted2() throws UnsupportedEncodingException {
        IASString s = new IASString("h.e-l*l_o World");

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    public void testEncodingUntainted3() throws UnsupportedEncodingException {
        IASString s = new IASString("hellü");

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    public void testEncodingUntainted4() throws UnsupportedEncodingException {
        IASString s = new IASString("hellࠀ");

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertFalse(encode.isTainted());
    }

    @Test
    public void testEncodingTainted1() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hel");
        IASString s2 = new IASString("lo");
        s2.setTaint(true);
        IASString s = s1.concat(s2);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{new IASTaintRange(3, 5, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)}, encode.getTaintRanges().toArray());
    }

    @Test
    public void testEncodingTainted2() throws UnsupportedEncodingException {
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
                new IASTaintRange(1, 2, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(3, 4, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(5, 6, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(7, 8, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(9, 15, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
        }, encode.getTaintRanges().toArray());
    }

    @Test
    public void testEncodingTainted3() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        IASString s2 = new IASString("ü");
        s2.setTaint(true);
        IASString s3 = new IASString(" world");
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(4, 10, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintRanges().toArray());
    }

    @Test
    public void testEncodingTainted4() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        IASString s2 = new IASString("ࠀ");
        s2.setTaint(true);
        IASString s3 = new IASString(" world");
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(4, 13, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintRanges().toArray());
    }

    @Test
    public void testEncodingTainted5() throws UnsupportedEncodingException {
        IASString s1 = new IASString("hell");
        s1.setTaint(true);
        IASString s2 = new IASString("ࠀ");
        IASString s3 = new IASString(" world");
        s3.setTaint(true);
        IASString s = s1.concat(s2).concat(s3);

        IASString encode = TURLEncoder.encode(s, cs);

        assertEquals(URLEncoder.encode(s.getString(), csString), encode.getString());
        assertArrayEquals(new IASTaintRange[]{
                new IASTaintRange(0, 4, IASTaintSource.TS_CS_UNKNOWN_ORIGIN),
                new IASTaintRange(13, 19, IASTaintSource.TS_CS_UNKNOWN_ORIGIN)
        }, encode.getTaintRanges().toArray());
    }
}
