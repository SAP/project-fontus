package de.tubs.cs.ias.asm_test.taintaware.untainted;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TURLEncoder {
    @Deprecated
    public static IASString encode(IASString url) {
        return new IASString(URLEncoder.encode(url.getString()));
    }

    public static IASString encode(IASString url, IASString enc) throws UnsupportedEncodingException {
        return new IASString(URLEncoder.encode(url.getString(), enc.getString()));
    }
}
