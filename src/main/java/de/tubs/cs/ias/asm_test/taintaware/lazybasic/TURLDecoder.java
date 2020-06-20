package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASURLDecoder;

import java.io.UnsupportedEncodingException;

public class TURLDecoder {
    @Deprecated
    public static IASString decode(IASString url) {
        return (IASString) IASURLDecoder.decode(url, new IASFactoryImpl());
    }

    public static IASString decode(IASString url, IASString enc) throws UnsupportedEncodingException {
        return (IASString) IASURLDecoder.decode(url, enc, new IASFactoryImpl());
    }
}
