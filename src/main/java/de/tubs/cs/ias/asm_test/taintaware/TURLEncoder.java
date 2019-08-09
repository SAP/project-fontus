package de.tubs.cs.ias.asm_test.taintaware;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TURLEncoder {
    @Deprecated
    static IASString encode(IASString url) {
        boolean taint = url.isTainted();
        String encoded = URLEncoder.encode(url.getString());
        return new IASString(encoded, taint);
    }

    static IASString encode(IASString url, IASString enc) throws UnsupportedEncodingException {
        boolean taint = url.isTainted();
        String encoded = URLEncoder.encode(url.getString(), enc.getString());
        return new IASString(encoded, taint);
    }
}
