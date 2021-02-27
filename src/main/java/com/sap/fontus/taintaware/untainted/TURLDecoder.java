package com.sap.fontus.taintaware.untainted;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TURLDecoder {
    @Deprecated
    public static IASString decode(IASString url) {
        return new IASString(URLDecoder.decode(url.getString()));
    }

    public static IASString decode(IASString url, IASString enc) throws UnsupportedEncodingException {
        return new IASString(URLDecoder.decode(url.getString(), enc.getString()));
    }
}
