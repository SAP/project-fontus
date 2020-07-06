package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.util.List;
import java.util.stream.IntStream;

public final class IASStringBuilder extends IASAbstractStringBuilder{
    public IASStringBuilder() {
        super();
    }

    public IASStringBuilder(int capacity) {
        super(capacity);
    }

    public IASStringBuilder(CharSequence seq) {
        super(seq);
    }

    public IASStringBuilder(IASStringable string) {
        super(string);
    }

    public IASStringBuilder(IASStringBuilderable strb) {
        super(strb);
    }

    public IASStringBuilder(StringBuffer buffer) {
        super(buffer);
    }

    public IASStringBuilder(IASString string) {
        super(string);
    }

    @Override
    public IASStringBuilder append(Object obj) {
        return (IASStringBuilder) super.append(obj);
    }

    @Override
    public IASStringBuilder append(IASStringable toAppend) {
        return (IASStringBuilder) super.append(toAppend);
    }

    @Override
    public IASStringBuilder append(IASStringBuilderable toAppend) {
        return (IASStringBuilder) super.append(toAppend);
    }

    @Override
    public IASTaintInformation getTaintInformation() {
        return super.getTaintInformation();
    }

    @Override
    public IASStringBuilder append(char[] s, int offset, int len) {
        return (IASStringBuilder) super.append(s, offset, len);
    }

    @Override
    public IASStringBuilder append(char[] str) {
        return (IASStringBuilder) super.append(str);
    }

    @Override
    public IASStringBuilder append(boolean b) {
        return (IASStringBuilder) super.append(b);
    }

    @Override
    public IASStringBuilder append(int i) {
        return (IASStringBuilder) super.append(i);
    }

    @Override
    public IASStringBuilder append(CharSequence charSequence) {
        return (IASStringBuilder) super.append(charSequence);
    }

    @Override
    public IASStringBuilder append(CharSequence charSequence, int start, int end) {
        return (IASStringBuilder) super.append(charSequence, start, end);
    }

    @Override
    public IASStringBuilder append(char c) {
        return (IASStringBuilder) super.append(c);
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
    public IASStringBuilder appendCodePoint(int codePoint) {
        return (IASStringBuilder) super.appendCodePoint(codePoint);
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
    public int capacity() {
        return super.capacity();
    }

    @Override
    public IASString substring(int start) {
        return super.substring(start);
    }

    @Override
    public IASString substring(int start, int end) {
        return super.substring(start, end);
    }

    @Override
    public void setCharAt(int index, char c) {
        super.setCharAt(index, c);
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
    public StringBuilder getStringBuilder() {
        return super.getStringBuilder();
    }

    @Override
    public void setLength(int newLength) {
        super.setLength(newLength);
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        return super.getTaintRanges();
    }

    @Override
    public boolean isUninitialized() {
        return super.isUninitialized();
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public boolean isTaintedAt(int index) {
        return super.isTaintedAt(index);
    }

    @Override
    public void setTaint(IASTaintSource source) {
        super.setTaint(source);
    }

    @Override
    public boolean isTainted() {
        return super.isTainted();
    }

    @Override
    public void setTaint(boolean taint) {
        super.setTaint(taint);
    }

    @Override
    public int length() {
        return super.length();
    }

    @Override
    public char charAt(int i) {
        return super.charAt(i);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return super.subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return super.chars();
    }

    @Override
    public IntStream codePoints() {
        return super.codePoints();
    }

    @Override
    public int compareTo(IASStringBuilderable iasStringBuilderable) {
        return super.compareTo(iasStringBuilderable);
    }
}
