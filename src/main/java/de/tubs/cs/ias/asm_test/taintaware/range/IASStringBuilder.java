package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintInformation;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintRange;

import java.util.ArrayList;
import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public final class IASStringBuilder implements java.io.Serializable, Comparable<IASStringBuilder>, CharSequence, IASTaintAware {

    private final StringBuilder builder;
    private final IASTaintInformation taintInformation;

    public IASStringBuilder(StringBuilder sb, IASTaintInformation taintInformation) {
        this.builder = new StringBuilder(sb);
        this.taintInformation = taintInformation;
    }

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

    public IASStringBuilder() {
        this.builder = new StringBuilder();
        this.taintInformation = new IASTaintInformation();
    }

    public IASStringBuilder(int capacity) {
        this.builder = new StringBuilder(capacity);
        this.taintInformation = new IASTaintInformation();
    }

    public IASStringBuilder(IASString str) {
        this.builder = new StringBuilder();
        this.taintInformation = new IASTaintInformation();
        this.append(str);
    }

    public IASStringBuilder(StringBuilder sb) {
        this.builder = new StringBuilder(sb);
        this.taintInformation = new IASTaintInformation();
    }

    public IASStringBuilder(CharSequence seq) {
        this.builder = new StringBuilder();
        this.taintInformation = new IASTaintInformation();
        IASString iasString = IASString.valueOf(seq);
        this.append(iasString);
    }

    public IASStringBuilder append(Object obj) {
        IASString iasString = IASString.valueOf(obj);
        this.append(iasString);
        return this;
    }

    public IASStringBuilder append(IASString str) {
        int size = this.length();
        this.taintInformation.appendRangesFrom(str.getTaintInformation(), size);
        this.builder.append(str.toString());
        return this;
    }

    public IASStringBuilder append(StringBuffer strb) {
        this.builder.append(strb);
        return this;
    }

    public IASStringBuilder append(IASStringBuffer strb) {
        this.builder.append(strb.getBuffer());
        this.taintInformation.appendRangesFrom(strb.getTaintInformation(), this.length());
        return this;
    }

    public IASStringBuilder append(CharSequence cs) {
        IASString iasString = IASString.valueOf(cs);
        return this.append(iasString);
    }

    public IASStringBuilder append(CharSequence s, int start, int end) {
        IASString iasString = IASString.valueOf(s);
        return this.append(iasString.substring(start, end));
    }

    public IASStringBuilder append(char[] s, int start, int end) {
        this.builder.append(s, start, end);
        return this;
    }

    public IASStringBuilder append(char[] str) {
        this.builder.append(str);
        return this;
    }

    public IASStringBuilder append(boolean b) {
        this.builder.append(b);
        return this;
    }

    public IASStringBuilder append(char c) {
        this.builder.append(c);
        return this;
    }

    public IASStringBuilder append(int i) {
        this.builder.append(i);
        return this;
    }

    public IASStringBuilder append(long lng) {
        this.builder.append(lng);
        return this;
    }

    public IASStringBuilder append(float f) {
        this.builder.append(f);
        return this;
    }

    public IASStringBuilder append(double d) {
        this.builder.append(d);
        return this;
    }

    public IASStringBuilder appendCodePoint(int codePoint) {
        this.builder.appendCodePoint(codePoint);
        return this;
    }

    public IASStringBuilder delete(int start, int end) {
        this.builder.delete(start, end);
        this.taintInformation.removeTaintFor(start, end, true);
        return this;
    }

    public IASStringBuilder deleteCharAt(int index) {
        this.builder.deleteCharAt(index);
        this.taintInformation.removeTaintFor(index, index + 1, true);
        return this;
    }

    public IASStringBuilder replace(int start, int end, IASString str) {
        this.builder.replace(start, end, str.toString());
        this.taintInformation.replaceTaintInformation(start, end, str.getTaintInformation().getAllRanges(), str.length());
        return this;
    }

    public IASStringBuilder insert(int index, char[] str, int offset,
                                   int len) {
        IASString iasString = IASString.valueOf(str, offset, len);
        this.insert(index, iasString);
        return this;
    }

    public IASStringBuilder insert(int offset, Object obj) {
        IASString iasString = IASString.valueOf(obj);
        this.insert(offset, iasString);
        return this;
    }

    public IASStringBuilder insert(int offset, IASString str) {
        this.taintInformation.insert(offset, new ArrayList<IASTaintRange>(0), str.length());
        this.builder.insert(offset, str.toString());
        return this;
    }

    public IASStringBuilder insert(int offset, char[] str) {
        this.insert(offset, str, 0, str.length);
        return this;
    }

    public IASStringBuilder insert(int dstOffset, CharSequence s) {
        this.insert(dstOffset, s, 0, s.length());
        return this;
    }

    public IASStringBuilder insert(int dstOffset, CharSequence s,
                                   int start, int end) {
        IASString iasString = IASString.valueOf(s);
        iasString = iasString.substring(start, end);
        this.insert(dstOffset, iasString);
        return this;
    }

    public IASStringBuilder insert(int offset, boolean b) {
        var s = IASString.valueOf(b);
        return this.insert(offset, s);
    }

    public IASStringBuilder insert(int offset, char c) {
        var s = IASString.valueOf(c);
        return this.insert(offset, s);
    }

    public IASStringBuilder insert(int offset, int i) {
        var s = IASString.valueOf(i);
        return this.insert(offset, s);
    }

    public IASStringBuilder insert(int offset, long l) {
        var s = IASString.valueOf(l);
        return this.insert(offset, s);
    }

    public IASStringBuilder insert(int offset, float f) {
        var s = IASString.valueOf(f);
        return this.insert(offset, s);
    }

    public IASStringBuilder insert(int offset, double d) {
        var s = IASString.valueOf(d);
        return this.insert(offset, s);
    }

    public int indexOf(String str) {
        return this.builder.indexOf(str);
    }

    public int indexOf(IASString str, int fromIndex) {
        return this.builder.indexOf(str.toString(), fromIndex);
    }

    public int lastIndexOf(IASString str) {
        return this.builder.lastIndexOf(str.toString());
    }

    public int lastIndexOf(IASString str, int fromIndex) {
        return this.builder.lastIndexOf(str.toString(), fromIndex);
    }

    public IASStringBuilder reverse() {
        var newTaintInfo = this.taintInformation.reversed(this.length());
        var newBuilder = new IASStringBuilder(this.builder.reverse(), newTaintInfo);
        return newBuilder;
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }

    public IASString toIASString() {
        return new IASString(this.builder.toString(), this.taintInformation.copy());
    }

    public int capacity() {
        return this.builder.capacity();
    }

    public IASString substring(int start) {
        return this.toIASString().substring(start);
    }

    public IASString substring(int start, int end) {
        return this.toIASString().substring(start, end);
    }

    public void setCharAt(int index, char c) {
        this.builder.setCharAt(index, c);
    }

    public void ensureCapacity(int minimumCapacity) {
        this.builder.ensureCapacity(minimumCapacity);
    }

    public void trimToSize() {
        this.builder.trimToSize();
    }

    @Override
    public int length() {
        return this.builder.length();
    }

    @Override
    public char charAt(int index) {
        return this.builder.charAt(index);
    }

    // TODO: unsound
    @Override
    public CharSequence subSequence(int start, int end) {
        return this.toIASString().subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return this.builder.chars();
    }

    @Override
    public IntStream codePoints() {
        return this.builder.codePoints();
    }

    public StringBuilder getBuilder() {
        return this.builder;
    }

    public void setLength(int newLength) {
        this.builder.setLength(newLength);
        this.taintInformation.resize(0, newLength, 0);
    }

    @Override
    public int compareTo(IASStringBuilder o) {
        return this.builder.compareTo(o.builder);
    }

    public IASTaintInformation getTaintInformation() {
        return this.taintInformation;
    }

    public boolean isUninitialized() {
        return false;
    }
}
