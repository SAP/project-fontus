package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.Factory;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

public class LazyBasicFactory implements Factory {
    @Override
    public IASStringable createString(String s) {
        return new IASString(s);
    }

    @Override
    public IASStringable createString(String s, int taintRangeCount) {
        return new IASString(s, randomTaintRanges(s.length(), taintRangeCount));
    }

    @Override
    public IASStringable createRandomString(int length) {
        return new IASString(randomString(length));
    }

    @Override
    public IASStringable createRandomString(int length, int taintRangeCount) {
        return new IASString(randomString(length), randomTaintRanges(length, taintRangeCount));
    }
}
