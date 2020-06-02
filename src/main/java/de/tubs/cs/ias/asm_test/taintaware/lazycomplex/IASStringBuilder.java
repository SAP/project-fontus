package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.List;
import java.util.stream.IntStream;

public class IASStringBuilder extends IASAbstractStringBuilder {
    public IASStringBuilder() {
        super();
    }

    public IASStringBuilder(int capacity) {
        super(capacity);
    }

    public IASStringBuilder(CharSequence seq) {
        super(seq);
    }

    public IASStringBuilder(IASString string) {
        super(string);
    }

    @Override
    public synchronized List<IASTaintRange> getTaintRanges() {
        return super.getTaintRanges();
    }

    @Override
    public synchronized IASStringBuilderable append(Object obj) {
        return super.append(obj);
    }

    @Override
    public synchronized IASStringBuilderable append(IASStringable str) {
        return super.append(str);
    }

    @Override
    public synchronized IASStringBuilderable append(StringBuffer strb) {
        return super.append(strb);
    }

    @Override
    public synchronized IASStringBuilderable append(CharSequence seq) {
        return super.append(seq);
    }

    @Override
    public synchronized IASStringBuilderable append(CharSequence seq, int start, int end) {
        return super.append(seq, start, end);
    }

    @Override
    public synchronized IASStringBuilderable append(char[] s, int offset, int len) {
        return super.append(s, offset, len);
    }

    @Override
    public synchronized IASStringBuilderable append(char[] chars) {
        return super.append(chars);
    }

    @Override
    public synchronized IASStringBuilderable append(boolean b) {
        return super.append(b);
    }

    @Override
    public synchronized IASStringBuilderable append(int i) {
        return super.append(i);
    }

    @Override
    public synchronized IASStringBuilderable append(long lng) {
        return super.append(lng);
    }

    @Override
    public synchronized IASStringBuilderable append(float f) {
        return super.append(f);
    }

    @Override
    public synchronized IASStringBuilderable append(char c) {
        return super.append(c);
    }

    @Override
    public synchronized IASStringBuilderable append(double d) {
        return super.append(d);
    }

    @Override
    public synchronized IASStringBuilderable appendCodePoint(int codePoint) {
        return super.appendCodePoint(codePoint);
    }

    @Override
    public synchronized IASStringBuilderable delete(int start, int end) {
        return super.delete(start, end);
    }

    @Override
    public synchronized IASStringBuilderable deleteCharAt(int index) {
        return super.deleteCharAt(index);
    }

    @Override
    public synchronized IASStringBuilderable replace(int start, int end, IASStringable str) {
        return super.replace(start, end, str);
    }

    @Override
    public synchronized IASStringBuilderable insert(int index, char[] str, int offset, int len) {
        return super.insert(index, str, offset, len);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, Object obj) {
        return super.insert(offset, obj);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, IASStringable str) {
        return super.insert(offset, str);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, char[] str) {
        return super.insert(offset, str);
    }

    @Override
    public synchronized IASStringBuilderable insert(int dstOffset, CharSequence s) {
        return super.insert(dstOffset, s);
    }

    @Override
    public synchronized IASStringBuilderable insert(int dstOffset, CharSequence s, int start, int end) {
        return super.insert(dstOffset, s, start, end);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, boolean b) {
        return super.insert(offset, b);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, char c) {
        return super.insert(offset, c);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, int i) {
        return super.insert(offset, i);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, long l) {
        return super.insert(offset, l);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, float f) {
        return super.insert(offset, f);
    }

    @Override
    public synchronized IASStringBuilderable insert(int offset, double d) {
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
    public synchronized IASStringBuilderable reverse() {
        return super.reverse();
    }

    @Override
    public synchronized IASStringable toIASString() {
        return super.toIASString();
    }

    @Override
    public synchronized int capacity() {
        return super.capacity();
    }

    @Override
    public synchronized IASStringable substring(int start) {
        return super.substring(start);
    }

    @Override
    public synchronized IASStringable substring(int start, int end) {
        return super.substring(start, end);
    }

    @Override
    public synchronized void setCharAt(int index, char c) {
        super.setCharAt(index, c);
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
    public synchronized char charAt(int index) {
        return super.charAt(index);
    }

    @Override
    public synchronized CharSequence subSequence(int start, int end) {
        return super.subSequence(start, end);
    }

    @Override
    public synchronized IntStream chars() {
        return super.chars();
    }

    @Override
    public synchronized IntStream codePoints() {
        return super.codePoints();
    }

    @Override
    public synchronized int compareTo(IASStringBuilderable o) {
        return super.compareTo(o);
    }
}
