package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.Factory;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

public class BoolFactory implements Factory {
    @Override
    public IASStringable createString(String s) {
        return new IASString(s);
    }

    @Override
    public IASStringable createString(String s, int taintRangeCount) {
        return new IASString(s, taintRangeCount > 0 && s.length() > 0);
    }

    @Override
    public IASStringable createRandomString(int length) {
        return new IASString(randomString(length));
    }

    @Override
    public IASStringable createRandomString(int length, int taintRangeCount) {
        return new IASString(randomString(length), taintRangeCount > 0 && length > 0);
    }
}
