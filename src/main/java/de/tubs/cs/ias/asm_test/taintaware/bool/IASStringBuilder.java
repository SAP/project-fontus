package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

public final class IASStringBuilder extends IASAbstractStringBuilder {
    @Override
    public boolean isTainted() {
        return super.isTainted();
    }

    @Override
    public void setTaint(boolean taint) {
        super.setTaint(taint);
    }

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

    @Override
    public int length() {
        return super.length();
    }

    @Override
    public int capacity() {
        return super.capacity();
    }

    @Override
    public void ensureCapacity(int minimumCapacity) {
        super.ensureCapacity(minimumCapacity);
    }

    @Override
    public void trimToSize() {
        super.trimToSize();
    }

    @Override
    public StringBuilder getBuilder() {
        return super.getBuilder();
    }

    @Override
    public void setLength(int newLength) {
        super.setLength(newLength);
    }

    @Override
    public char charAt(int index) {
        return super.charAt(index);
    }

    @Override
    public int codePointAt(int index) {
        return super.codePointAt(index);
    }

    @Override
    public int codePointBefore(int index) {
        return super.codePointBefore(index);
    }

    @Override
    public int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
    }

    @Override
    public int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
    }

    @Override
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        super.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public void setCharAt(int index, char ch) {
        super.setCharAt(index, ch);
    }

    @Override
    public IASAbstractStringBuilder append(Object obj) {
        return super.append(obj);
    }

    @Override
    public IASAbstractStringBuilder append(IASStringable str) {
        return super.append(str);
    }

    @Override
    public IASAbstractStringBuilder append(String str) {
        return super.append(str);
    }

    @Override
    public IASAbstractStringBuilder append(StringBuffer sb) {
        return super.append(sb);
    }

    @Override
    public IASAbstractStringBuilder append(IASAbstractStringBuilder sb) {
        return super.append(sb);
    }

    @Override
    IASAbstractStringBuilder append(IASStringBuilder asb) {
        return super.append(asb);
    }

    @Override
    public IASAbstractStringBuilder append(CharSequence csq) {
        return super.append(csq);
    }

    @Override
    public IASAbstractStringBuilder append(CharSequence csq, int start, int end) {
        return super.append(csq, start, end);
    }

    @Override
    public IASAbstractStringBuilder append(char[] str) {
        return super.append(str);
    }

    @Override
    public IASAbstractStringBuilder append(char[] str, int offset, int len) {
        return super.append(str, offset, len);
    }

    @Override
    public IASAbstractStringBuilder append(boolean b) {
        return super.append(b);
    }

    @Override
    public IASAbstractStringBuilder append(char c) {
        return super.append(c);
    }

    @Override
    public IASAbstractStringBuilder append(int i) {
        return super.append(i);
    }

    @Override
    public IASAbstractStringBuilder appendCodePoint(int codePoint) {
        return super.appendCodePoint(codePoint);
    }

    @Override
    public IASAbstractStringBuilder append(long lng) {
        return super.append(lng);
    }

    @Override
    public IASAbstractStringBuilder append(float f) {
        return super.append(f);
    }

    @Override
    public IASAbstractStringBuilder append(double d) {
        return super.append(d);
    }

    @Override
    public IASAbstractStringBuilder delete(int start, int end) {
        return super.delete(start, end);
    }

    @Override
    public IASAbstractStringBuilder deleteCharAt(int index) {
        return super.deleteCharAt(index);
    }

    @Override
    public IASAbstractStringBuilder replace(int start, int end, IASStringable str) {
        return super.replace(start, end, str);
    }

    @Override
    public IASString substring(int start) {
        return super.substring(start);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return super.subSequence(start, end);
    }

    @Override
    public IASString substring(int start, int end) {
        return super.substring(start, end);
    }

    @Override
    public IASAbstractStringBuilder insert(int index, char[] str, int offset, int len) {
        return super.insert(index, str, offset, len);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, Object obj) {
        return super.insert(offset, obj);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, IASStringable str) {
        return super.insert(offset, str);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, char[] str) {
        return super.insert(offset, str);
    }

    @Override
    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        return super.insert(dstOffset, s);
    }

    @Override
    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        return super.insert(dstOffset, s, start, end);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, boolean b) {
        return super.insert(offset, b);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, char c) {
        return super.insert(offset, c);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, int i) {
        return super.insert(offset, i);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, long l) {
        return super.insert(offset, l);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, float f) {
        return super.insert(offset, f);
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, double d) {
        return super.insert(offset, d);
    }

    @Override
    public int indexOf(IASStringable str) {
        return super.indexOf(str);
    }

    @Override
    public int indexOf(IASStringable str, int fromIndex) {
        return super.indexOf(str, fromIndex);
    }

    @Override
    public int lastIndexOf(IASStringable str) {
        return super.lastIndexOf(str);
    }

    @Override
    public int lastIndexOf(IASStringable str, int fromIndex) {
        return super.lastIndexOf(str, fromIndex);
    }

    @Override
    public IASAbstractStringBuilder reverse() {
        return super.reverse();
    }

    @Override
    public IASString toIASString() {
        return super.toIASString();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int compareTo(IASStringBuilderable o) {
        return super.compareTo(o);
    }
}
