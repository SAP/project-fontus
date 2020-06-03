package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TURLEncoder {
    @Deprecated
    public static IASString encode(IASString url) {
        boolean taint = url.isTainted();
        String encoded = URLEncoder.encode(url.getString());
        return new IASString(encoded, taint);
    }

    public static IASString encode(IASString url, IASString enc) throws UnsupportedEncodingException {
        boolean taint = url.isTainted();
        String encoded = URLEncoder.encode(url.getString(), enc.getString());
        return new IASString(encoded, taint);
    }
}
