package de.tubs.cs.ias.asm_test.taintaware;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TURLDecoder {
    @Deprecated
    static IASString decode(IASString url) {
        boolean taint = url.isTainted();
        String decoded = URLDecoder.decode(url.getString());
        return new IASString(decoded, taint);
    }

    static IASString decode(IASString url, IASString enc) throws UnsupportedEncodingException {
        boolean taint = url.isTainted();
        String decoded = URLDecoder.decode(url.getString(), enc.getString());
        return new IASString(decoded, taint);
    }
}
