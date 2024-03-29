package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.IASTaintAware;

@SuppressWarnings("Since15")
public final class IASStringBuilder extends IASAbstractStringBuilder {

    public IASStringBuilder(StringBuilder sb, IASTaintInformationable taintInformation) {
        super();
        this.appendShifted(taintInformation, sb.length());
        this.stringBuilder.append(sb);
    }

    public IASStringBuilder(StringBuilder sb) {
        super(sb);
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

    public IASStringBuilder(CharSequence seq) {
        super(seq);
    }

    public static IASStringBuilder fromStringBuilder(StringBuilder param) {
        if (param == null) {
            return null;
        }
        return new IASStringBuilder(param);
    }

    public IASStringBuilder append(Object obj) {
        return (IASStringBuilder) super.append(obj);
    }

    public IASStringBuilder append(IASString str) {
        return (IASStringBuilder) super.append(str);
    }

    public IASStringBuilder append(IASStringBuilder strb) {
        return (IASStringBuilder) super.append(strb);
    }

    public IASStringBuilder append(IASStringBuffer strb) {
        return (IASStringBuilder) super.append(strb);
    }

    public IASStringBuilder append(CharSequence cs) {
        return (IASStringBuilder) super.append(cs);
    }

    public IASStringBuilder append(CharSequence s, int start, int end) {
        return (IASStringBuilder) super.append(s, start, end);
    }

    public IASStringBuilder append(char[] s, int offset, int len) {
        return (IASStringBuilder) super.append(s, offset, len);
    }

    public IASStringBuilder append(char[] str) {
        return (IASStringBuilder) super.append(str);
    }

    public IASStringBuilder append(boolean b) {
        return (IASStringBuilder) super.append(b);
    }

    public IASStringBuilder append(char c) {
        return (IASStringBuilder) super.append(c);
    }

    public IASStringBuilder append(int i) {
        return (IASStringBuilder) super.append(i);
    }

    public IASStringBuilder append(long lng) {
        return (IASStringBuilder) super.append(lng);
    }

    public IASStringBuilder append(float f) {
        return (IASStringBuilder) super.append(f);
    }

    public IASStringBuilder append(double d) {
        return (IASStringBuilder) super.append(d);
    }

    public IASStringBuilder appendCodePoint(int codePoint) {
        return (IASStringBuilder) super.appendCodePoint(codePoint);
    }

    public IASStringBuilder delete(int start, int end) {
        return (IASStringBuilder) super.delete(start, end);
    }

    public IASStringBuilder deleteCharAt(int index) {
        return (IASStringBuilder) super.deleteCharAt(index);
    }

    public IASStringBuilder replace(int start, int end, IASString str) {
        return (IASStringBuilder) super.replace(start, end, str);
    }

    public IASStringBuilder insert(int index, char[] str, int offset,
                                   int len) {
        return (IASStringBuilder) super.insert(index, str, offset, len);
    }

    public IASStringBuilder insert(int offset, Object obj) {
        return (IASStringBuilder) super.insert(offset, obj);
    }

    public IASStringBuilder insert(int offset, IASString str) {
        return (IASStringBuilder) super.insert(offset, str);
    }

    public IASStringBuilder insert(int offset, char[] str) {
        return (IASStringBuilder) super.insert(offset, str);
    }

    public IASStringBuilder insert(int dstOffset, CharSequence s) {
        return (IASStringBuilder) super.insert(dstOffset, s);
    }

    public IASStringBuilder insert(int dstOffset, CharSequence s,
                                   int start, int end) {
        return (IASStringBuilder) super.insert(dstOffset, s, start, end);
    }

    public IASStringBuilder insert(int offset, boolean b) {
        return (IASStringBuilder) super.insert(offset, b);
    }

    public IASStringBuilder insert(int offset, char c) {
        return (IASStringBuilder) super.insert(offset, c);
    }

    public IASStringBuilder insert(int offset, int i) {
        return (IASStringBuilder) super.insert(offset, i);
    }

    public IASStringBuilder insert(int offset, long l) {
        return (IASStringBuilder) super.insert(offset, l);
    }

    public IASStringBuilder insert(int offset, float f) {
        return (IASStringBuilder) super.insert(offset, f);
    }

    public IASStringBuilder insert(int offset, double d) {
        return (IASStringBuilder) super.insert(offset, d);
    }

    public IASStringBuilder reverse() {
        return (IASStringBuilder) super.reverse();
    }

    public int compareTo(IASStringBuilder o) {
        return super.compareTo(o);
    }

    @Override
    public IASTaintAware copy() {
        return new IASStringBuilder(this);
    }

    @Override
    public IASTaintAware newInstance() {
        return new IASStringBuilder();
    }

}
