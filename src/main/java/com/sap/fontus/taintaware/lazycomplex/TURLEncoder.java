package com.sap.fontus.taintaware.lazycomplex;

import com.sap.fontus.taintaware.lazycomplex.operations.URLEncodingOperation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class TURLEncoder {
    @Deprecated
    public static IASString encode(IASString url) {
        return url.derive(URLEncoder.encode(url.getString()), new URLEncodingOperation(), false);
    }

    public static IASString encode(IASString url, IASString enc) throws UnsupportedEncodingException {
        return url.derive(URLEncoder.encode(url.getString(), enc.getString()), new URLEncodingOperation(Charset.forName(enc.getString())), false);
    }
}
