package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRanges;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.tubs.cs.ias.asm_test.taintaware.shared.IASURLEncoder.isValidChar;

public class URLEncodingOperation implements IASOperation {
    private final Charset charset;

    public URLEncodingOperation() {
        this.charset = Charset.defaultCharset();
    }

    public URLEncodingOperation(Charset charset) {
        this.charset = charset;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint) {
        if (previousTaint.isEmpty()) {
            return previousTaint;
        }

        IASTaintRanges taintRanges = new IASTaintRanges();
        boolean isValid = false;
        int start = 0;
        int currentLength = 0;
        for (int i = 0; i < previousString.length(); i++) {
            char c = previousString.charAt(i);
            if (isValid) {
                if (!isValidChar(c)) {
                    currentLength += encodeValid(previousTaint, taintRanges, start, i, currentLength);
                    isValid = false;
                    start = i;
                }
            } else {
                if (isValidChar(c)) {
                    currentLength += encodeNonValid(previousString, previousTaint, taintRanges, start, i, currentLength);
                    isValid = true;
                    start = i;
                }
            }
        }
        if (isValid) {
            encodeValid(previousTaint, taintRanges, start, previousString.length(), currentLength);
        } else {
            encodeNonValid(previousString, previousTaint, taintRanges, start, previousString.length(), currentLength);
        }
        return taintRanges.getAllRanges();
    }

    private int encodeValid(List<IASTaintRange> ranges, IASTaintRanges taintRanges, int start, int end, int currentLength) {
        ranges = new ArrayList<>(ranges);
        IASTaintRangeUtils.adjustAndRemoveRanges(ranges, start, end, start - currentLength);
        taintRanges.appendRanges(ranges, true);
        return end - start;
    }

    private int encodeNonValid(String url, List<IASTaintRange> ranges, IASTaintRanges taintRanges, int start, int end, int currentLength) {
        int origCurrentLength = currentLength;
        IASTaintRanges sTr = new IASTaintRanges(new ArrayList<>(ranges));
        for (int i = start; i < end; i++) {
            String s = url.substring(i, i + 1);
            IASTaintSource source = sTr.getTaintFor(i);
            if (s.equals(" ")) {
                if (source != null) {
                    taintRanges.appendRanges(Collections.singletonList(new IASTaintRange(currentLength, currentLength + 1, source)), true);
                }
                currentLength++;
            } else {
                byte[] bytes = s.getBytes(this.charset);
                int newCurrentLength = currentLength + bytes.length * 3;
                if(source != null) {

                    taintRanges.appendRanges(Collections.singletonList(new IASTaintRange(currentLength, newCurrentLength, source)), true);
                }
                currentLength = newCurrentLength;
            }
        }
        return currentLength - origCurrentLength;
    }
}
