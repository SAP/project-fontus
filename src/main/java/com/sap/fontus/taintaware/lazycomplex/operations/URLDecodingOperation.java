package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.range.IASString;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;
import com.sap.fontus.taintaware.range.IASFactoryImpl;
import com.sap.fontus.taintaware.shared.IASURLDecoder;

import java.nio.charset.Charset;
import java.util.List;

public class URLDecodingOperation implements IASOperation {
    private final Charset charset;

    public URLDecodingOperation() {
        this.charset = Charset.defaultCharset();
    }

    public URLDecodingOperation(Charset charset) {
        this.charset = charset;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint) {
        if (previousTaint.isEmpty()) {
            return previousTaint;
        }

        IASString url = new IASString(previousString, previousTaint);
        IASString result = (IASString) IASURLDecoder.decode(url, this.charset, new IASFactoryImpl());
        return result.getTaintRanges();
    }
}
