package de.tubs.cs.ias.asm_test.taintaware.bool;


import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.util.stream.IntStream;

@SuppressWarnings({"SynchronizedMethod", "ReturnOfThis", "WeakerAccess", "ClassWithTooManyConstructors", "ClassWithTooManyMethods"})
public final class IASStringBuffer extends IASAbstractStringBuilder {

    public IASStringBuffer() {
        super();
    }

    public IASStringBuffer(int capacity) {
        super(capacity);
    }

    public IASStringBuffer(IASString str) {
        super(str);
    }

    public IASStringBuffer(String str) {
        super(str);
    }

    public IASStringBuffer(CharSequence seq) {
        super(seq);
    }

    public IASStringBuffer(StringBuffer buffer) {
        super(buffer);
    }

    @Override
    public synchronized boolean isTainted() {
        return super.isTainted();
    }

    @Override
    public synchronized void setTaint(boolean taint) {
        super.setTaint(taint);
    }

    @Override
    public synchronized int length() {
        return super.length();
    }

    @Override
    public synchronized int capacity() {
        return super.capacity();
    }

    @Override
    public synchronized void ensureCapacity(int minimumCapacity) {
        super.ensureCapacity(minimumCapacity);
    }

    @Override
    public synchronized void trimToSize() {
        super.trimToSize();
    }

    @Override
    public synchronized StringBuilder getBuilder() {
        return super.getBuilder();
    }

    @Override
    public synchronized void setLength(int newLength) {
        super.setLength(newLength);
    }

    @Override
    public synchronized char charAt(int index) {
        return super.charAt(index);
    }

    @Override
    public synchronized int codePointAt(int index) {
        return super.codePointAt(index);
    }

    @Override
    public synchronized int codePointBefore(int index) {
        return super.codePointBefore(index);
    }

    @Override
    public synchronized int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
    }

    @Override
    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
    }

    @Override
    public synchronized void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        super.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public synchronized void setCharAt(int index, char ch) {
        super.setCharAt(index, ch);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(Object obj) {
        return super.append(obj);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(IASStringable str) {
        return super.append(str);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(String str) {
        return super.append(str);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(StringBuffer sb) {
        return super.append(sb);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(IASAbstractStringBuilder sb) {
        return super.append(sb);
    }

    @Override
    IASAbstractStringBuilder append(IASStringBuilder asb) {
        return super.append(asb);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(CharSequence csq) {
        return super.append(csq);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(CharSequence csq, int start, int end) {
        return super.append(csq, start, end);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(char[] str) {
        return super.append(str);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(char[] str, int offset, int len) {
        return super.append(str, offset, len);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(boolean b) {
        return super.append(b);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(char c) {
        return super.append(c);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(int i) {
        return super.append(i);
    }

    @Override
    public synchronized IASAbstractStringBuilder appendCodePoint(int codePoint) {
        return super.appendCodePoint(codePoint);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(long lng) {
        return super.append(lng);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(float f) {
        return super.append(f);
    }

    @Override
    public synchronized IASAbstractStringBuilder append(double d) {
        return super.append(d);
    }

    @Override
    public synchronized IASAbstractStringBuilder delete(int start, int end) {
        return super.delete(start, end);
    }

    @Override
    public synchronized IASAbstractStringBuilder deleteCharAt(int index) {
        return super.deleteCharAt(index);
    }

    @Override
    public synchronized IASAbstractStringBuilder replace(int start, int end, IASStringable str) {
        return super.replace(start, end, str);
    }

    @Override
    public synchronized IASStringable substring(int start) {
        return super.substring(start);
    }

    @Override
    public synchronized CharSequence subSequence(int start, int end) {
        return super.subSequence(start, end);
    }

    @Override
    public synchronized IASStringable substring(int start, int end) {
        return super.substring(start, end);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int index, char[] str, int offset, int len) {
        return super.insert(index, str, offset, len);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, Object obj) {
        return super.insert(offset, obj);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, IASStringable str) {
        return super.insert(offset, str);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, char[] str) {
        return super.insert(offset, str);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        return super.insert(dstOffset, s);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        return super.insert(dstOffset, s, start, end);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, boolean b) {
        return super.insert(offset, b);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, char c) {
        return super.insert(offset, c);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, int i) {
        return super.insert(offset, i);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, long l) {
        return super.insert(offset, l);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, float f) {
        return super.insert(offset, f);
    }

    @Override
    public synchronized IASAbstractStringBuilder insert(int offset, double d) {
        return super.insert(offset, d);
    }

    @Override
    public synchronized int indexOf(IASStringable str) {
        return super.indexOf(str);
    }

    @Override
    public synchronized int indexOf(IASStringable str, int fromIndex) {
        return super.indexOf(str, fromIndex);
    }

    @Override
    public synchronized int lastIndexOf(IASStringable str) {
        return super.lastIndexOf(str);
    }

    @Override
    public synchronized int lastIndexOf(IASStringable str, int fromIndex) {
        return super.lastIndexOf(str, fromIndex);
    }

    @Override
    public synchronized IASAbstractStringBuilder reverse() {
        return super.reverse();
    }

    @Override
    public synchronized IASStringable toIASString() {
        return super.toIASString();
    }

    @Override
    public synchronized String toString() {
        return super.toString();
    }

    @Override
    public synchronized int compareTo(IASStringBuilderable o) {
        return super.compareTo(o);
    }

    @Override
    public synchronized IntStream chars() {
        return null;
    }

    @Override
    public synchronized IntStream codePoints() {
        return null;
    }
}
