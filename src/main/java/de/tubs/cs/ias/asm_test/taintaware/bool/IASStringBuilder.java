package de.tubs.cs.ias.asm_test.taintaware.bool;

public final class IASStringBuilder extends IASAbstractStringBuilder {
    public IASStringBuilder() {
        super();
    }

    public IASStringBuilder(int capacity) {
        super(capacity);
    }

    public IASStringBuilder(IASString str) {
        super(str);
    }

    public IASStringBuilder(String str) {
        super(str);
    }

    public IASStringBuilder(CharSequence seq) {
        super(seq);
    }

    public IASStringBuilder(StringBuffer buffer) {
        super(buffer);
    }
}
