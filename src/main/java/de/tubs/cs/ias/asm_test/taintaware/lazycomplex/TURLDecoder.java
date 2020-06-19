package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations.URLDecodingOperation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class TURLDecoder {
    @Deprecated
    public static IASString decode(IASString url) {
        return url.derive(URLDecoder.decode(url.getString()), new URLDecodingOperation(), false);
    }

    public static IASString decode(IASString url, IASString enc) throws UnsupportedEncodingException {
        return url.derive(URLDecoder.decode(url.getString(), enc.getString()), new URLDecodingOperation(Charset.forName(enc.getString())), false);
    }
}
