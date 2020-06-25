package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface Factory {
    static final String ABC = "abcdefghijklmnopqrstuvwxyz";

    IASStringable createString(String s);

    IASStringable createString(String s, int taintRangeCount);

    IASStringable createRandomString(int length);

    IASStringable createRandomString(int length, int taintRangeCount);

    default String randomString(int size) {
        int repetitions = (int) Math.ceil(((double) size) / ABC.length());
        String res = String.join("", Collections.nCopies(repetitions, ABC));
        return res.substring(0, size);
    }

    default List<IASTaintRange> randomTaintRanges(int stringSize, int count) {
        if (stringSize < count) {
            throw new IllegalArgumentException("String size cannot be smaller than taint range count. String size: " + stringSize + " taint range count: " + count);
        }

        if (count == 0) {
            return Collections.emptyList();
        }

        int width = stringSize / count;
        List<IASTaintRange> ranges = new ArrayList<>(count);

        for (int start = 0, end = width, i = 0; i < count; start += width, end += width, i++) {
            ranges.add(new IASTaintRange(start, end, (short) (i + 1)));
        }

        return ranges;
    }
}
