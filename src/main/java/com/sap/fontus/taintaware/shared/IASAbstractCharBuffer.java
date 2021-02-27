package com.sap.fontus.taintaware.shared;

public class IASAbstractCharBuffer {
    private final IASAbstractStringBuilderable stringBuffer;
    private int position = 0;
    private final int limit;

    public IASAbstractCharBuffer(IASStringable string, IASFactory factory) {
        this.stringBuffer = factory.createStringBuilder(string);
        this.limit = string.length();
    }

    public int position() {
        return this.position;
    }

    public boolean hasRemaining() {
        return position < limit;
    }

    public char get() {
        if (!hasRemaining()) {
            throw new IllegalStateException("Nothing remaining");
        }
        char c = this.stringBuffer.charAt(this.position);
        this.position++;
        return c;
    }

    public void rewind() {
        position = 0;
    }

    public IASStringable subSequence(int start, int end) {
        return stringBuffer.substring(start, end);
    }

    public IASAbstractCharBuffer position(int newPosition) {
        this.position = newPosition;
        return this;
    }

    public int limit() {
        return this.limit;
    }
}
