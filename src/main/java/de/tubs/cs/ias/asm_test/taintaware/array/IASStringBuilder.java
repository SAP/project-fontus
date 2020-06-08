package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

import java.util.stream.IntStream;

@SuppressWarnings("Since15")
public final class IASStringBuilder extends IASAbstractStringBuilder {

    public IASStringBuilder(StringBuilder sb, IASTaintInformation taintInformation) {
        super();
        this.taintInformation.setTaint(this.length(), taintInformation.getTaints());
        this.builder.append(sb);
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

    public IASStringBuilder(IASStringable str) {
        super(str);
    }

    public IASStringBuilder(CharSequence seq) {
        super(seq);
    }

    public IASStringBuilder(IASStringBuilderable s) {
        super(s);
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
    public IASStringBuilder append(StringBuffer strb) {
        return (IASStringBuilder) super.append(strb);
    }

    @Override
    public IASStringBuilder append(IASStringBuilderable strb) {
        return (IASStringBuilder) super.append(strb);
    }

    @Override
    public IASStringBuilder append(CharSequence cs) {
        return (IASStringBuilder) super.append(cs);
    }

    @Override
    public IASStringBuilder append(CharSequence s, int start, int end) {
        return (IASStringBuilder) super.append(s, start, end);
    }

    @Override
    public IASStringBuilder append(char[] s, int start, int end) {
        return (IASStringBuilder) super.append(s, start, end);
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
    public IASStringBuilder append(char c) {
        return (IASStringBuilder) super.append(c);
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
