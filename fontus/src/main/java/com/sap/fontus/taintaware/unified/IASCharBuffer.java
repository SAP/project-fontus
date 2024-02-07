package com.sap.fontus.taintaware.unified;

public class IASCharBuffer {
    private final IASStringBuffer stringBuffer;
    private int position = 0;
    private final int limit;

    public IASCharBuffer(IASString string) {
        this.stringBuffer = new IASStringBuffer(string);
        this.limit = string.length();
    }

    public int position() {
        return this.position;
    }

    public boolean hasRemaining() {
        return this.position < this.limit;
    }

    public char get() {
        if (!this.hasRemaining()) {
            throw new IllegalStateException("Nothing remaining");
        }
        char c = this.stringBuffer.charAt(this.position);
        this.position++;
        return c;
    }

    public void rewind() {
        this.position = 0;
    }

    public IASString subSequence(int start, int end) {
        return this.stringBuffer.substring(start, end);
    }

    public IASCharBuffer position(int newPosition) {
        this.position = newPosition;
        return this;
    }

    public int limit() {
        return this.limit;
    }
}
