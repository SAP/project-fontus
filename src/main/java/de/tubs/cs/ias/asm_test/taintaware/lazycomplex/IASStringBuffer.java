package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

public class IASStringBuffer extends IASAbstractStringBuilder {
    public IASStringBuffer() {
        super();
    }

    public IASStringBuffer(int capacity) {
        super(capacity);
    }

    public IASStringBuffer(CharSequence seq) {
        super(seq);
    }

    public IASStringBuffer(IASString string) {
        super(string);
    }
}
