package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintInformation;

import java.util.ArrayList;

@SuppressWarnings({"SynchronizedMethod", "ReturnOfThis", "WeakerAccess", "ClassWithTooManyConstructors", "ClassWithTooManyMethods", "unused"})
public final class IASStringBuffer
        implements java.io.Serializable, CharSequence, IASRangeAware, Comparable<IASStringBuffer> {

    // TODO: accessed in both synchronized and unsynchronized methods
    private final StringBuffer buffer;
    private final IASTaintInformation taintInformation;

    public IASStringBuffer(StringBuffer buffer, IASTaintInformation taintInformation) {
        this.buffer = new StringBuffer(buffer);
        this.taintInformation = taintInformation.copy();
    }

    @Override
    public boolean isTainted() {
        return this.taintInformation.isTainted();
    }

    @Override
    public void setTaint(boolean taint) {
        if (!this.isTainted()) {
            var end = this.buffer.length();
            this.taintInformation.addRange(0, end, (short) 0);
        }
    }

    public IASStringBuffer() {
        this.buffer = new StringBuffer();
        this.taintInformation = new IASTaintInformation();
    }

    public IASStringBuffer(int capacity) {
        this.buffer = new StringBuffer(capacity);
        this.taintInformation = new IASTaintInformation();
    }

    public IASStringBuffer(IASString str) {
        this.buffer = new StringBuffer(str.getString());
        this.taintInformation = new IASTaintInformation(str.getTaintInformation().getAllRanges());
    }

    public IASStringBuffer(String str) {
        this.buffer = new StringBuffer(str);
        this.taintInformation = new IASTaintInformation();
    }


    public IASStringBuffer(CharSequence seq) {
        var str = IASString.valueOf(seq);
        this.buffer = new StringBuffer(seq);
        this.taintInformation = new IASTaintInformation(str.getTaintInformation().getAllRanges());
    }

    public IASStringBuffer(StringBuffer buffer) {
        this.buffer = new StringBuffer(buffer);
        this.taintInformation = new IASTaintInformation();
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
        var str = IASString.valueOf(obj);
        return this.append(str);
    }

    public synchronized IASStringBuffer append(IASString str) {
        var shift = this.length();
        this.taintInformation.appendRangesFrom(str.getTaintInformation(), shift);
        this.buffer.append(str.toString());
        return this;
    }

    public synchronized IASStringBuffer append(String str) {
        this.buffer.append(str);
        return this;
    }

    public synchronized IASStringBuffer append(IASStringBuffer sb) {
        this.taintInformation.appendRangesFrom(sb.getTaintInformation(), this.length());
        this.buffer.append(sb);
        return this;
    }

    // TODO: Add the abstract base class
    synchronized IASStringBuffer append(IASStringBuilder asb) {
        this.taintInformation.appendRangesFrom(asb.getTaintInformation(), this.length());
        this.buffer.append(asb);
        return this;
    }


    public synchronized IASStringBuffer append(CharSequence s) {
        var str = IASString.valueOf(s);
        return this.append(str);
    }


    public synchronized IASStringBuffer append(CharSequence s, int start, int end) {
        var str = IASString.valueOf(s, start, end);
        return this.append(str);
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
        this.taintInformation.removeTaintFor(start, end, true);
        return this;
    }


    public synchronized IASStringBuffer deleteCharAt(int index) {
        this.buffer.deleteCharAt(index);
        this.taintInformation.removeTaintFor(index, index + 1, true);
        return this;
    }


    public synchronized IASStringBuffer replace(int start, int end, IASString str) {
        this.buffer.replace(start, end, str.toString());
        this.taintInformation.replaceTaintInformation(start, end, str.getTaintInformation().getAllRanges(), str.length(), true);
        return this;
    }

    public synchronized IASString substring(int start) {
        return this.toIASString().substring(start);
    }


    public synchronized CharSequence subSequence(int start, int end) {
        return this.toIASString().subSequence(start, end);
    }


    public synchronized IASString substring(int start, int end) {
        return this.toIASString().substring(start, end);
    }

    public synchronized IASStringBuffer insert(int index, char[] str, int offset,
                                               int len) {
        this.buffer.insert(index, str, offset, len);
        this.taintInformation.insert(index, new ArrayList<>(0), len);
        return this;
    }

    public synchronized IASStringBuffer insert(int offset, Object obj) {
        var str = IASString.valueOf(obj);
        return this.insert(offset, str);
    }

    public synchronized IASStringBuffer insert(int offset, IASString str) {
        this.buffer.insert(offset, str.toString());
        this.taintInformation.insert(offset, str.getTaintInformation().getAllRanges(), str.length());
        return this;
    }

    public synchronized IASStringBuffer insert(int offset, char[] str) {
        this.buffer.insert(offset, str);
        this.taintInformation.insert(offset, new ArrayList<>(0), str.length);
        return this;
    }

    public IASStringBuffer insert(int dstOffset, CharSequence s) {
        var str = IASString.valueOf(s);
        return this.insert(dstOffset, str);
    }

    public synchronized IASStringBuffer insert(int dstOffset, CharSequence s,
                                               int start, int end) {
        var str = IASString.valueOf(s, start, end);
        return this.insert(dstOffset, str);
    }

    public IASStringBuffer insert(int offset, boolean b) {
        var str = IASString.valueOf(b);
        return this.insert(offset, str);
    }

    public synchronized IASStringBuffer insert(int offset, char c) {
        var str = IASString.valueOf(c);
        return this.insert(offset, str);
    }

    public IASStringBuffer insert(int offset, int i) {
        var str = IASString.valueOf(i);
        return this.insert(offset, str);
    }

    public IASStringBuffer insert(int offset, long l) {
        var str = IASString.valueOf(l);
        return this.insert(offset, str);
    }

    public IASStringBuffer insert(int offset, float f) {
        var str = IASString.valueOf(f);
        return this.insert(offset, str);
    }

    public IASStringBuffer insert(int offset, double d) {
        var str = IASString.valueOf(d);
        return this.insert(offset, str);
    }

    public int indexOf(IASString str) {
        return this.buffer.indexOf(str.toString());
    }

    public synchronized int indexOf(IASString str, int fromIndex) {
        return this.buffer.indexOf(str.toString(), fromIndex);
    }

    public int lastIndexOf(IASString str) {
        return this.buffer.lastIndexOf(str.toString());
    }

    public synchronized int lastIndexOf(IASString str, int fromIndex) {
        return this.buffer.lastIndexOf(str.getString(), fromIndex);
    }

    public synchronized IASStringBuffer reverse() {
        this.buffer.reverse();
        this.taintInformation.reversed(this.length());
        return this;
    }

    public synchronized IASString toIASString() {
        return new IASString(this.buffer.toString(), this.taintInformation.getAllRanges());
    }

    public synchronized String toString() {
        return this.buffer.toString();
    }

    public StringBuffer getBuffer() {
        return this.buffer;
    }

    @Override
    public int compareTo(IASStringBuffer o) {
        return this.buffer.compareTo(o.buffer);
    }

    public IASTaintInformation getTaintInformation() {
        return this.taintInformation;
    }

    public boolean isUninitialized() {
        return !isTainted();
    }
}
