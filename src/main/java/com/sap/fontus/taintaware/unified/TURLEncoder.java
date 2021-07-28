package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.shared.IASURLEncoder;

import java.io.UnsupportedEncodingException;

public class TURLEncoder {
    @Deprecated
    public static IASString encode(IASString url) {
        return (IASString) IASURLEncoder.encode(url, new IASFactoryImpl());
    }

    public static IASString encode(IASString url, IASString enc) throws UnsupportedEncodingException {
        return (IASString) IASURLEncoder.encode(url, enc, new IASFactoryImpl());
    }
}
