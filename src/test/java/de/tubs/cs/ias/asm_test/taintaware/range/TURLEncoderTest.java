package de.tubs.cs.ias.asm_test.taintaware.range;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;

@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
class TURLEncoderTest {
    @Test
    void testUntainted() throws UnsupportedEncodingException {
        IASString s = new IASString("hello");

        IASString s2 = TURLEncoder.encode(s, new IASString(StandardCharsets.UTF_8.toString()));

        assertFalse(s2.isTainted());
    }
}
