package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASURLEncoder;

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
