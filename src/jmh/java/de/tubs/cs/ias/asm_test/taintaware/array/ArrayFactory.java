package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.Factory;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

public class ArrayFactory implements Factory {
    @Override
    public IASStringable createString(String s) {
        return new IASString(s);
    }

    @Override
    public IASStringable createString(String s, int taintRangeCount) {
        return new IASString(s, TaintConverter.toTaintArray(s.length(), randomTaintRanges(s.length(), taintRangeCount)));
    }

    @Override
    public IASStringable createRandomString(int length) {
        return new IASString(randomString(length));
    }

    @Override
    public IASStringable createRandomString(int length, int taintRangeCount) {
        return new IASString(randomString(length), TaintConverter.toTaintArray(length, randomTaintRanges(length, taintRangeCount)));
    }
}
