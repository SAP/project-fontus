package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

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

    public IASStringBuilder(IASStringable str) {
        super(str);
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
    public IASStringBuilder append(Object obj) {
        return (IASStringBuilder) super.append(obj);
    }

    @Override
    public IASStringBuilder append(IASStringable str) {
        return (IASStringBuilder) super.append(str);
    }

    @Override
    public IASStringBuilder append(String str) {
        return (IASStringBuilder) super.append(str);
    }

    @Override
    public IASStringBuilder append(IASStringBuilderable asb) {
        return (IASStringBuilder) super.append(asb);
    }

    @Override
    public IASStringBuilder append(CharSequence csq) {
        return (IASStringBuilder) super.append(csq);
    }

    @Override
    public IASStringBuilder append(CharSequence csq, int start, int end) {
        return (IASStringBuilder) super.append(csq, start, end);
    }

    @Override
    public IASStringBuilder append(char[] str) {
        return (IASStringBuilder) super.append(str);
    }

    @Override
    public IASStringBuilder append(char[] str, int offset, int len) {
        return (IASStringBuilder) super.append(str, offset, len);
    }

    @Override
    public IASStringBuilder append(boolean b) {
        return (IASStringBuilder) super.append(b);
    }

    @Override
    public IASStringBuilder append(char c) {
        return (IASStringBuilder) super.append(c);
    }

    @Override
    public IASStringBuilder append(int i) {
        return (IASStringBuilder) super.append(i);
    }

    @Override
    public IASStringBuilder appendCodePoint(int codePoint) {
        return (IASStringBuilder) super.appendCodePoint(codePoint);
    }

    @Override
    public IASStringBuilder append(long lng) {
        return (IASStringBuilder) super.append(lng);
    }

    @Override
    public IASStringBuilder append(float f) {
        return (IASStringBuilder) super.append(f);
    }

    @Override
    public IASStringBuilder append(double d) {
        return (IASStringBuilder) super.append(d);
    }

    @Override
    public IASStringBuilder delete(int start, int end) {
        return (IASStringBuilder) super.delete(start, end);
    }

    @Override
    public IASStringBuilder deleteCharAt(int index) {
        return (IASStringBuilder) super.deleteCharAt(index);
    }

    @Override
    public IASStringBuilder replace(int start, int end, IASStringable str) {
        return (IASStringBuilder) super.replace(start, end, str);
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
    public IASStringBuilder insert(int index, char[] str, int offset, int len) {
        return (IASStringBuilder) super.insert(index, str, offset, len);
    }

    @Override
    public IASStringBuilder insert(int offset, Object obj) {
        return (IASStringBuilder) super.insert(offset, obj);
    }

    @Override
    public IASStringBuilder insert(int offset, IASStringable str) {
        return (IASStringBuilder) super.insert(offset, str);
    }

    @Override
    public IASStringBuilder insert(int offset, char[] str) {
        return (IASStringBuilder) super.insert(offset, str);
    }

    @Override
    public IASStringBuilder insert(int dstOffset, CharSequence s) {
        return (IASStringBuilder) super.insert(dstOffset, s);
    }

    @Override
    public IASStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        return (IASStringBuilder) super.insert(dstOffset, s, start, end);
    }

    @Override
    public IASStringBuilder insert(int offset, boolean b) {
        return (IASStringBuilder) super.insert(offset, b);
    }

    @Override
    public IASStringBuilder insert(int offset, char c) {
        return (IASStringBuilder) super.insert(offset, c);
    }

    @Override
    public IASStringBuilder insert(int offset, int i) {
        return (IASStringBuilder) super.insert(offset, i);
    }

    @Override
    public IASStringBuilder insert(int offset, long l) {
        return (IASStringBuilder) super.insert(offset, l);
    }

    @Override
    public IASStringBuilder insert(int offset, float f) {
        return (IASStringBuilder) super.insert(offset, f);
    }

    @Override
    public IASStringBuilder insert(int offset, double d) {
        return (IASStringBuilder) super.insert(offset, d);
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
    public IASStringBuilder reverse() {
        return (IASStringBuilder) super.reverse();
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