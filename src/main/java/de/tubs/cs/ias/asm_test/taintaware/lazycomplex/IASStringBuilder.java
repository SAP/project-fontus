package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

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

    public IASStringBuilder(StringBuilder stringBuilder) {
        this((CharSequence) stringBuilder);
    }

    public IASStringBuilder(IASStringable string) {
        super(string);
    }

    public IASStringBuilder(IASStringBuilderable strb) {
        super(strb);
    }

    public IASStringBuilder(IASString string) {
        super(string);
    }

    public IASStringBuilder(IASStringBuilder strb) {
        super(strb);
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
    public IASStringBuilder append(IASStringBuilderable strb) {
        return (IASStringBuilder) super.append(strb);
    }

    @Override
    public IASStringBuilder append(CharSequence seq) {
        return (IASStringBuilder) super.append(seq);
    }

    @Override
    public IASStringBuilder append(CharSequence seq, int start, int end) {
        return (IASStringBuilder) super.append(seq, start, end);
    }

    @Override
    public IASStringBuilder append(char[] s, int offset, int len) {
        return (IASStringBuilder) super.append(s, offset, len);
    }

    @Override
    public IASStringBuilder append(char[] chars) {
        return (IASStringBuilder) super.append(chars);
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
    public IASStringBuilder append(long lng) {
        return (IASStringBuilder) super.append(lng);
    }

    @Override
    public IASStringBuilder append(float f) {
        return (IASStringBuilder) super.append(f);
    }

    @Override
    public IASStringBuilder append(char c) {
        return (IASStringBuilder) super.append(c);
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
    public IASStringBuilder reverse() {
        return (IASStringBuilder) super.reverse();
    }
}
