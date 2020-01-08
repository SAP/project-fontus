package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASString;
import de.tubs.cs.ias.asm_test.taintaware.IASStringBuffer;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintInformation;

import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public final class IASStringBuilderTaintRange implements java.io.Serializable, Comparable<IASStringBuilderTaintRange>, CharSequence, IASTaintAware {

    private final StringBuilder builder;
    private IASTaintInformation taintInformation;

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

    public IASStringBuilderTaintRange() {
        this.builder = new StringBuilder();
        this.tainted = false;
    }

    public IASStringBuilderTaintRange(int capacity) {
        this.builder = new StringBuilder(capacity);
        this.tainted = false;
    }

    public IASStringBuilderTaintRange(IASString str) {
        if(str == null) {
            this.builder = new StringBuilder(str);
        } else {
            this.builder = new StringBuilder(str.getString());
            this.mergeTaint(str);
        }
    }

    public IASStringBuilderTaintRange(StringBuilder sb) {
        this.builder = sb; //TODO: shared instance okay?
    }

    public IASStringBuilderTaintRange(CharSequence seq) {
        this.builder = new StringBuilder(seq);
        if(seq instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) seq;
            this.mergeTaint(ta);
        }
    }

    public IASStringBuilderTaintRange append(Object obj) {
        this.builder.append(obj);
        return this;
    }

    public IASStringBuilderTaintRange append(IASString str) {
        if(str == null) {
            this.builder.append(str);
            return this;
        }
        this.builder.append(str.getString());
        this.mergeTaint(str);
        return this;
    }

    public IASStringBuilderTaintRange append(StringBuffer strb) {
        this.builder.append(strb);
        return this;
    }
    public IASStringBuilderTaintRange append(IASStringBuffer strb) {
        if(strb == null) {
            this.builder.append(strb);
            return this;
        }
        this.mergeTaint(strb);
        this.builder.append(strb.getBuffer());
        return this;
    }
    public IASStringBuilderTaintRange append(CharSequence cs) {
        if(cs instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) cs;
            this.mergeTaint(ta);
        }
        this.builder.append(cs);
        return this;
    }

    public IASStringBuilderTaintRange append(CharSequence s, int start, int end) {
        this.builder.append(s, start, end);
        return this;
    }

    public IASStringBuilderTaintRange append(char[] s, int start, int end) {
        this.builder.append(s, start, end);
        return this;
    }

    public IASStringBuilderTaintRange append(char[] str) {
        this.builder.append(str);
        return this;
    }

    public IASStringBuilderTaintRange append(boolean b) {
        this.builder.append(b);
        return this;
    }

    public IASStringBuilderTaintRange append(char c) {
        this.builder.append(c);
        return this;
    }

    public IASStringBuilderTaintRange append(int i) {
        this.builder.append(i);
        return this;
    }

    public IASStringBuilderTaintRange append(long lng) {
        this.builder.append(lng);
        return this;
    }

    public IASStringBuilderTaintRange append(float f) {
        this.builder.append(f);
        return this;
    }

    public IASStringBuilderTaintRange append(double d) {
        this.builder.append(d);
        return this;
    }

    public IASStringBuilderTaintRange appendCodePoint(int codePoint) {
        this.builder.appendCodePoint(codePoint);
        return this;
    }

    public IASStringBuilderTaintRange delete(int start, int end) {
        this.builder.delete(start, end);
        if(this.builder.length() == 0) {
            this.tainted = false;
        }
        return this;
    }

    public IASStringBuilderTaintRange deleteCharAt(int index) {
        this.builder.deleteCharAt(index);
        if(this.builder.length() == 0) {
            this.tainted = false;
        }
        return this;
    }

    public IASStringBuilderTaintRange replace(int start, int end, IASString str) {
        this.mergeTaint(str);
        this.builder.replace(start, end, str.getString());
        return this;
    }

    public IASStringBuilderTaintRange insert(int index, char[] str, int offset,
                                             int len) {
        this.builder.insert(index, str, offset, len);
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, Object obj) {
        this.builder.insert(offset, obj);
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, IASString str) {
        this.mergeTaint(str);
        this.builder.insert(offset, str.getString());
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, char[] str) {
        this.builder.insert(offset, str);
        return this;
    }

    public IASStringBuilderTaintRange insert(int dstOffset, CharSequence s) {
        if(s instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) s;
            this.mergeTaint(ta);
        }
        this.builder.insert(dstOffset, s);
        return this;
    }

    public IASStringBuilderTaintRange insert(int dstOffset, CharSequence s,
                                             int start, int end) {
        if(s instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) s;
            this.mergeTaint(ta);
        }
        this.builder.insert(dstOffset, s, start, end);
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, boolean b) {
        this.builder.insert(offset, b);
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, char c) {
        this.builder.insert(offset, c);
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, int i) {
        this.builder.insert(offset, i);
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, long l) {
        this.builder.insert(offset, l);
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, float f) {
        this.builder.insert(offset, f);
        return this;
    }

    public IASStringBuilderTaintRange insert(int offset, double d) {
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

    public IASStringBuilderTaintRange reverse() {
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

    public int capacity()  {
        return this.builder.capacity();
    }

    public IASString substring(int start) {
        String substr = this.builder.substring(start);
        return new IASString(substr, this.isTainted());
    }

    public IASString substring(int start, int end) {
        String substr = this.builder.substring(start, end);
        return new IASString(substr, this.isTainted());
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
        IASStringBuilderTaintRange sb = new IASStringBuilderTaintRange(this.builder.subSequence(start, end));
        sb.tainted = this.tainted;
        return sb;
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
        return builder;
    }

    public void setLength(int newLength) {
        this.builder.setLength(newLength);
        // TODO: need to trim the taint once it is character based
    }

    @Override
    public int compareTo(IASStringBuilderTaintRange o) {
        return this.builder.compareTo(o.builder);
    }

    public IASTaintInformation getTaintInformation() {
        return this.taintInformation;
    }
}
