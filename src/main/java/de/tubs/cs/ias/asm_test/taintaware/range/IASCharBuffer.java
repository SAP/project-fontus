package de.tubs.cs.ias.asm_test.taintaware.range;

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
        return position < limit;
    }

    public IASChar get() {
        if (!hasRemaining()) {
            throw new IllegalStateException("Nothing remaining");
        }
        IASChar c = this.stringBuffer.getIASCharAt(this.position);
        this.position++;
        return c;
    }

    public void rewind() {
        position = 0;
    }

    public IASString subSequence(int start, int end) {
        return stringBuffer.substring(start, end);
    }

    public IASCharBuffer position(int newPosition) {
        this.position = newPosition;
        return this;
    }

    public int limit() {
        return this.limit;
    }
}
