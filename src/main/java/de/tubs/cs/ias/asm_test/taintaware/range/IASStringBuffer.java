package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintInformation;

@SuppressWarnings({"SynchronizedMethod", "ReturnOfThis", "WeakerAccess", "ClassWithTooManyConstructors", "ClassWithTooManyMethods"})
public final class IASStringBuffer
        implements java.io.Serializable, CharSequence, IASTaintAware, Comparable<IASStringBuffer> {

    // TODO: accessed in both synchronized and unsynchronized methods
    private final StringBuffer buffer;
    private IASTaintInformation taintInformation;

    @Override
    public boolean isTainted() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void setTaint(boolean taint) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    private void mergeTaint(IASTaintAware other) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer(int capacity) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer(IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer(String str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public IASStringBuffer(CharSequence seq) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer(StringBuffer buffer) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
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
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(String str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(IASStringBuffer sb) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    // TODO: Add the abstract base class
    synchronized IASStringBuffer append(IASStringBuilder asb) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public synchronized IASStringBuffer append(CharSequence s) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public synchronized IASStringBuffer append(CharSequence s, int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(char[] str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(char[] str, int offset, int len) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(boolean b) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(char c) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(int i) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer appendCodePoint(int codePoint) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public synchronized IASStringBuffer append(long lng) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(float f) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer append(double d) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public synchronized IASStringBuffer delete(int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public synchronized IASStringBuffer deleteCharAt(int index) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public synchronized IASStringBuffer replace(int start, int end, IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASString substring(int start) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public synchronized CharSequence subSequence(int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public synchronized IASString substring(int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer insert(int index, char[] str, int offset,
                                               int len) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer insert(int offset, Object obj) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer insert(int offset, IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer insert(int offset, char[] str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer insert(int dstOffset, CharSequence s) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer insert(int dstOffset, CharSequence s,
                                               int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer insert(int offset, boolean b) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASStringBuffer insert(int offset, char c) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer insert(int offset, int i) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer insert(int offset, long l) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer insert(int offset, float f) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuffer insert(int offset, double d) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public int indexOf(IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized int indexOf(IASString str, int fromIndex) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public int lastIndexOf(IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized int lastIndexOf(IASString str, int fromIndex) {
        return this.buffer.lastIndexOf(str.getString(), fromIndex);
    }

    public synchronized IASStringBuffer reverse() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized IASString toIASString() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public synchronized String toString() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public StringBuffer getBuffer() {
        return this.buffer;
    }

    @Override
    public int compareTo(IASStringBuffer o) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASTaintInformation getTaintInformation() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }
}
