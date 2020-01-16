package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintInformation;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintRange;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static de.tubs.cs.ias.asm_test.taintaware.range.IASTaintRangeUtils.adjustRanges;

@SuppressWarnings("unused")
public abstract class IASAbstractStringBuilder implements java.io.Serializable, Comparable<IASStringBuilder>, CharSequence, IASRangeAware {
    protected final StringBuilder builder;
    protected final IASTaintInformation taintInformation;

    public IASAbstractStringBuilder() {
        this.builder = new StringBuilder();
        this.taintInformation = new IASTaintInformation();
    }

    public IASAbstractStringBuilder(int capacity) {
        this.builder = new StringBuilder(capacity);
        this.taintInformation = new IASTaintInformation();
    }

    public IASAbstractStringBuilder(IASString str) {
        this.builder = new StringBuilder(str);
        this.taintInformation = new IASTaintInformation(str.getTaintInformation().getAllRanges());
    }

    public IASAbstractStringBuilder(CharSequence seq) {
        var str = IASString.valueOf(seq);
        this.builder = new StringBuilder(str);
        this.taintInformation = new IASTaintInformation(str.getTaintInformation().getAllRanges());
    }

    @Override
    public void setTaint(boolean taint) {
        if (!this.isTainted()) {
            this.taintInformation.addRange(0, this.length(), (short) 0);
        }
    }

    @Override
    public boolean isTainted() {
        return this.taintInformation.isTainted();
    }

    public IASAbstractStringBuilder append(Object obj) {
        IASString iasString = IASString.valueOf(obj);
        this.append(iasString);
        return this;
    }

    public IASAbstractStringBuilder append(IASString str) {
        return this.append(str, false);
    }


    public IASAbstractStringBuilder append(IASString str, boolean merge) {
        var leftShift = -this.length();

        var ranges = str.getTaintInformation().getAllRanges();
        adjustRanges(ranges, 0, str.length(), leftShift);
        this.taintInformation.appendRanges(ranges, merge);

        this.builder.append(str.toString());
        return this;
    }

    public IASAbstractStringBuilder append(StringBuffer strb) {
        this.builder.append(strb);
        return this;
    }

    public IASAbstractStringBuilder append(IASStringBuffer strb) {
        var leftShift = -this.length();

        var ranges = strb.getTaintInformation().getAllRanges();
        adjustRanges(ranges, 0, strb.length(), leftShift);
        this.taintInformation.appendRanges(ranges);

        this.builder.append(strb.toString());

        return this;
    }

    public IASAbstractStringBuilder append(CharSequence cs) {
        IASString iasString = IASString.valueOf(cs);
        return this.append(iasString);
    }

    public IASAbstractStringBuilder append(CharSequence s, int start, int end) {
        IASString iasString = IASString.valueOf(s);
        return this.append(iasString.substring(start, end));
    }

    public IASAbstractStringBuilder append(char[] s, int start, int end) {
        this.builder.append(s, start, end);
        return this;
    }

    public IASAbstractStringBuilder append(char[] str) {
        this.builder.append(str);
        return this;
    }

    public IASAbstractStringBuilder append(boolean b) {
        this.builder.append(b);
        return this;
    }

    public IASAbstractStringBuilder append(char c) {
        this.builder.append(c);
        return this;
    }

    public IASAbstractStringBuilder append(int i) {
        this.builder.append(i);
        return this;
    }

    public IASAbstractStringBuilder append(long lng) {
        this.builder.append(lng);
        return this;
    }

    public IASAbstractStringBuilder append(float f) {
        this.builder.append(f);
        return this;
    }

    public IASAbstractStringBuilder append(double d) {
        this.builder.append(d);
        return this;
    }

    public IASAbstractStringBuilder appendCodePoint(int codePoint) {
        this.builder.appendCodePoint(codePoint);
        return this;
    }

    public IASAbstractStringBuilder delete(int start, int end) {
        this.builder.delete(start, end);
        this.taintInformation.removeTaintFor(start, end, true);
        return this;
    }

    public IASAbstractStringBuilder deleteCharAt(int index) {
        this.builder.deleteCharAt(index);
        this.taintInformation.removeTaintFor(index, index + 1, true);
        return this;
    }

    public IASAbstractStringBuilder replace(int start, int end, IASString str) {
        this.builder.replace(start, end, str.toString());
        this.taintInformation.replaceTaintInformation(start, end, str.getTaintInformation().getAllRanges(), str.length(), true);
        return this;
    }

    public IASAbstractStringBuilder insert(int index, char[] str, int offset,
                              int len) {
        IASString iasString = IASString.valueOf(str, offset, len);
        this.insert(index, iasString);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, Object obj) {
        IASString iasString = IASString.valueOf(obj);
        this.insert(offset, iasString);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, IASString str) {
        this.taintInformation.insert(offset, str.getTaintInformation().getAllRanges(), str.length());
        this.builder.insert(offset, str.toString());
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, char[] str) {
        this.insert(offset, str, 0, str.length);
        return this;
    }

    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        this.insert(dstOffset, s, 0, s.length());
        return this;
    }

    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s,
                              int start, int end) {
        IASString iasString = IASString.valueOf(s);
        iasString = iasString.substring(start, end);
        this.insert(dstOffset, iasString);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, boolean b) {
        var s = IASString.valueOf(b);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, char c) {
        var s = IASString.valueOf(c);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, int i) {
        var s = IASString.valueOf(i);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, long l) {
        var s = IASString.valueOf(l);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, float f) {
        var s = IASString.valueOf(f);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, double d) {
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

    public IASAbstractStringBuilder reverse() {
        this.builder.reverse();
        this.taintInformation.reversed(this.length());
        handleSurrogatesForReversed();

        return this;
    }

    private void handleSurrogatesForReversed() {
        var chars = this.toString().toCharArray();
        for (int i = 0; i < this.length() - 1; i++) {
            char highSur = chars[i];
            char lowSur = chars[i + 1];
            if (Character.isLowSurrogate(lowSur) && Character.isHighSurrogate(highSur)) {
                var oldHighRange = this.taintInformation.cutTaint(i);
                var oldLowRange = this.taintInformation.cutTaint(i + 1);

                var ranges = new ArrayList<IASTaintRange>(2);

                if (oldLowRange != null) {
                    var newHighRange = oldLowRange.shiftRight(-1);
                    ranges.add(newHighRange);
                }
                if (oldHighRange != null) {
                    var newLowRange = oldHighRange.shiftRight(1);
                    ranges.add(newLowRange);
                }

                this.taintInformation.replaceTaintInformation(i, i + 2, ranges, 2, false);
            }
        }
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
        return !isTainted();
    }
}
