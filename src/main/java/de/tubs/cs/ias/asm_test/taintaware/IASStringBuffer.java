package de.tubs.cs.ias.asm_test.taintaware;


@SuppressWarnings({"SynchronizedMethod", "ReturnOfThis", "WeakerAccess", "ClassWithTooManyConstructors", "ClassWithTooManyMethods"})
public class IASStringBuffer
        implements java.io.Serializable, CharSequence {

    // TODO: accessed in both synchronized and unsynchronized methods
    private final StringBuffer buffer;
    private boolean tainted = false;

    public boolean isTainted() {
        return this.tainted;
    }

    public IASStringBuffer() {
        this.buffer = new StringBuffer();
    }

    public IASStringBuffer(int capacity) {

        this.buffer = new StringBuffer(capacity);

    }

    public IASStringBuffer(IASString str) {
        this.buffer = new StringBuffer(str.length() + 16);
        this.buffer.append(str);
        this.tainted = str.isTainted();
    }

    public IASStringBuffer(String str) {

        this.buffer = new StringBuffer(str.length() + 16);
        this.buffer.append(str);
    }


    public IASStringBuffer(CharSequence seq) {
        this.buffer = new StringBuffer(seq.length() + 16);
        this.buffer.append(seq);
    }

    public IASStringBuffer(StringBuffer buffer) {
        this.buffer = buffer; //TODO: do a deep copy? Can something mess us up as this is shared?
        this.tainted = false;
    }


    public synchronized int length() {
        return this.buffer.length();
    }


    public synchronized int capacity() {
        return this.buffer.capacity();
    }


    public synchronized void ensureCapacity(int minimumCapacity) {
        this.buffer.ensureCapacity(minimumCapacity);
    }


    public synchronized void trimToSize() {
        this.buffer.trimToSize();
    }


    public synchronized void setLength(int newLength) {
        this.buffer.setLength(newLength);
    }


    public synchronized char charAt(int index) {
        return this.buffer.charAt(index);
    }


    public synchronized int codePointAt(int index) {
        return this.buffer.codePointAt(index);
    }


    public synchronized int codePointBefore(int index) {
        return this.buffer.codePointBefore(index);
    }


    public synchronized int codePointCount(int beginIndex, int endIndex) {
        return this.buffer.codePointCount(beginIndex, endIndex);
    }


    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        return this.buffer.offsetByCodePoints(index, codePointOffset);
    }


    public synchronized void getChars(int srcBegin, int srcEnd, char[] dst,
                                      int dstBegin) {
        this.buffer.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public synchronized void setCharAt(int index, char ch) {
        this.buffer.setCharAt(index, ch);
    }

    public synchronized IASStringBuffer append(Object obj) {
        // TODO: fix?
        this.buffer.append(String.valueOf(obj));
        return this;
    }

    public synchronized IASStringBuffer append(IASString str) {
        if(str == null) {
            String s = null;
            this.buffer.append(s);
            return this;
        }
        this.buffer.append(str.toIASString());
        this.tainted |= str.isTainted();
        return this;
    }

    public synchronized IASStringBuffer append(String str) {
        this.buffer.append(str);
        return this;
    }

    public synchronized IASStringBuffer append(IASStringBuffer sb) {
        this.buffer.append(sb);
        this.tainted |= sb.tainted;
        return this;
    }

    // TODO: Add the abstract base class
    synchronized IASStringBuffer append(IASStringBuilder asb) {
        this.buffer.append(asb);
        this.tainted |= asb.isTainted();
        return this;
    }


    public synchronized IASStringBuffer append(CharSequence s) {
        this.buffer.append(s);
        return this;
    }


    public synchronized IASStringBuffer append(CharSequence s, int start, int end) {
        this.buffer.append(s, start, end);
        return this;
    }

    public synchronized IASStringBuffer append(char[] str) {
        this.buffer.append(str);
        return this;
    }

    public synchronized IASStringBuffer append(char[] str, int offset, int len) {
        this.buffer.append(str, offset, len);
        return this;
    }

    public synchronized IASStringBuffer append(boolean b) {
        this.buffer.append(b);
        return this;
    }

    public synchronized IASStringBuffer append(char c) {
        this.buffer.append(c);
        return this;
    }

    public synchronized IASStringBuffer append(int i) {
        this.buffer.append(i);
        return this;
    }

    public synchronized IASStringBuffer appendCodePoint(int codePoint) {
        this.buffer.appendCodePoint(codePoint);
        return this;
    }


    public synchronized IASStringBuffer append(long lng) {
        this.buffer.append(lng);
        return this;
    }

    public synchronized IASStringBuffer append(float f) {
        this.buffer.append(f);
        return this;
    }

    public synchronized IASStringBuffer append(double d) {
        this.buffer.append(d);
        return this;
    }


    public synchronized IASStringBuffer delete(int start, int end) {
        this.buffer.delete(start, end);
        return this;
    }


    public synchronized IASStringBuffer deleteCharAt(int index) {
        this.buffer.deleteCharAt(index);
        return this;
    }


    public synchronized IASStringBuffer replace(int start, int end, IASString str) {
        this.buffer.replace(start, end, str.getString());
        this.tainted |= str.isTainted();
        return this;
    }

    public synchronized IASString substring(int start) {
        return this.substring(start, this.buffer.length());
    }


    public synchronized CharSequence subSequence(int start, int end) {
        return this.buffer.substring(start, end);
    }


    public synchronized IASString substring(int start, int end) {
        return new IASString(this.buffer.substring(start, end), this.tainted);
    }

    public synchronized IASStringBuffer insert(int index, char[] str, int offset,
                                               int len) {
        this.buffer.insert(index, str, offset, len);
        return this;
    }

    public synchronized IASStringBuffer insert(int offset, Object obj) {
        this.buffer.insert(offset, String.valueOf(obj));
        return this;
    }

    public synchronized IASStringBuffer insert(int offset, IASString str) {
        this.buffer.insert(offset, str);
        return this;
    }

    public synchronized IASStringBuffer insert(int offset, char[] str) {
        this.buffer.insert(offset, str);
        return this;
    }

    public IASStringBuffer insert(int dstOffset, CharSequence s) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        // after narrowing of s to specific type
        // Ditto for toStringCache clearing
        this.buffer.insert(dstOffset, s);
        return this;
    }

    public synchronized IASStringBuffer insert(int dstOffset, CharSequence s,
                                               int start, int end) {
        this.buffer.insert(dstOffset, s, start, end);
        return this;
    }

    public IASStringBuffer insert(int offset, boolean b) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of b to String by super class method
        // Ditto for toStringCache clearing
        this.buffer.insert(offset, b);
        return this;
    }

    public synchronized IASStringBuffer insert(int offset, char c) {
        this.buffer.insert(offset, c);
        return this;
    }

    public IASStringBuffer insert(int offset, int i) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of i to String by super class method
        // Ditto for toStringCache clearing
        this.buffer.insert(offset, i);
        return this;
    }

    public IASStringBuffer insert(int offset, long l) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of l to String by super class method
        // Ditto for toStringCache clearing
        this.buffer.insert(offset, l);
        return this;
    }

    public IASStringBuffer insert(int offset, float f) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of f to String by super class method
        // Ditto for toStringCache clearing
        this.buffer.insert(offset, f);
        return this;
    }

    public IASStringBuffer insert(int offset, double d) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of d to String by super class method
        // Ditto for toStringCache clearing
        this.buffer.insert(offset, d);
        return this;
    }

    public int indexOf(IASString str) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        return this.buffer.indexOf(str.getString());
    }

    public synchronized int indexOf(IASString str, int fromIndex) {
        return this.buffer.indexOf(str.getString(), fromIndex);
    }

    public int lastIndexOf(IASString str) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        return this.lastIndexOf(str, this.buffer.length()); //TODO: correct?
    }

    public synchronized int lastIndexOf(IASString str, int fromIndex) {
        return this.buffer.lastIndexOf(str.getString(), fromIndex);
    }

    public synchronized IASStringBuffer reverse() {
        this.buffer.reverse();
        return this;
    }

    public synchronized IASString toIASString() {
        return new IASString(this.buffer.toString(), this.tainted);
    }

    public synchronized String toString() {
        return this.buffer.toString();
    }

    public StringBuffer getBuffer() {
        return this.buffer;
    }
}
