package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.range.IASFactoryImpl;
import de.tubs.cs.ias.asm_test.taintaware.range.IASString;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASURLDecoder;

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
