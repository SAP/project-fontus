package de.tubs.cs.ias.asm_test;

import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public class IASStringBuilder implements java.io.Serializable, /* Comparable<IASStringBuilder>, */ CharSequence, IASTaintAware {

    private final StringBuilder builder;
    private boolean tainted;

    @Override
    public boolean isTainted() {
        return this.tainted;
    }

    @Override
    public void setTaint(boolean taint) {
        this.tainted = taint;
    }
    private void mergeTaint(IASTaintAware str) {
        this.tainted |= str.isTainted();
    }

    public IASStringBuilder() {
        this.builder = new StringBuilder();
        this.tainted = false;
    }

    public IASStringBuilder(int capacity) {
        this.builder = new StringBuilder(capacity);
        this.tainted = false;
    }

    public IASStringBuilder(IASString str) {
        this.builder = new StringBuilder(str.getString());
        this.mergeTaint(str);
    }

    public IASStringBuilder(CharSequence seq) {
        this.builder = new StringBuilder(seq);
        this.tainted = false;
    }

    public IASStringBuilder append(Object obj) {
        this.builder.append(obj);
        return this;
    }

    public IASStringBuilder append(IASString str) {
        this.builder.append(str.getString());
        this.mergeTaint(str);
        return this;
    }

    public IASStringBuilder append(StringBuffer strb) {
        this.builder.append(strb);
        return this;
    }

    public IASStringBuilder append(CharSequence cs) {
        this.builder.append(cs);
        return this;
    }

    public IASStringBuilder append(CharSequence s, int start, int end) {
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
        return this;
    }

    public IASStringBuilder deleteCharAt(int index) {
        this.builder.deleteCharAt(index);
        return this;
    }

    public IASStringBuilder replace(int start, int end, String str) {
        this.builder.replace(start, end, str);
        return this;
    }

    public IASStringBuilder insert(int index, char[] str, int offset,
                                   int len) {
        this.builder.insert(index, str, offset, len);
        return this;
    }

    public IASStringBuilder insert(int offset, Object obj) {
        this.builder.insert(offset, obj);
        return this;
    }

    public IASStringBuilder insert(int offset, String str) {
        this.builder.insert(offset, str);
        return this;
    }

    public IASStringBuilder insert(int offset, char[] str) {
        this.builder.insert(offset, str);
        return this;
    }

    public IASStringBuilder insert(int dstOffset, CharSequence s) {
        this.builder.insert(dstOffset, s);
        return this;
    }

    public IASStringBuilder insert(int dstOffset, CharSequence s,
                                   int start, int end) {
        this.builder.insert(dstOffset, s, start, end);
        return this;
    }

    public IASStringBuilder insert(int offset, boolean b) {
        this.builder.insert(offset, b);
        return this;
    }

    public IASStringBuilder insert(int offset, char c) {
        this.builder.insert(offset, c);
        return this;
    }

    public IASStringBuilder insert(int offset, int i) {
        this.builder.insert(offset, i);
        return this;
    }

    public IASStringBuilder insert(int offset, long l) {
        this.builder.insert(offset, l);
        return this;
    }

    public IASStringBuilder insert(int offset, float f) {
        this.builder.insert(offset, f);
        return this;
    }

    public IASStringBuilder insert(int offset, double d) {
        this.builder.insert(offset, d);
        return this;
    }

    public int indexOf(String str) {
        return this.builder.indexOf(str);
    }

    public int indexOf(IASString str, int fromIndex) {
        return this.builder.indexOf(str.getString(), fromIndex);
    }

    public int lastIndexOf(IASString str) {
        return this.builder.lastIndexOf(str.getString());
    }

    public int lastIndexOf(IASString str, int fromIndex) {
        return this.builder.lastIndexOf(str.getString(), fromIndex);
    }

    public IASStringBuilder reverse() {
        this.builder.reverse();
        return this;
    }

    @Override
    public String toString() {
        // TODO: how to deal with this getting called?
        return this.builder.toString();
    }

    public IASString toIASString() {
        return new IASString(this.builder.toString(), this.tainted);
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
        return this.builder.subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return this.builder.chars();
    }

    @Override
    public IntStream codePoints() {
        return this.builder.codePoints();
    }

    /* @Override
    public int compareTo(IASStringBuilder o) {
        return this.builder.compareTo(o.builder);
    }*/
}
