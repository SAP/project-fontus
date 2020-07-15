package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.io.Serializable;

public interface IASAbstractStringBuilderable extends Serializable, Comparable<IASAbstractStringBuilderable>, Appendable, CharSequence, IASTaintAware {
    IASAbstractStringBuilderable append(Object obj);

    IASAbstractStringBuilderable append(IASStringable str);

    IASAbstractStringBuilderable append(IASAbstractStringBuilderable str);

    IASAbstractStringBuilderable append(char[] s, int offset, int len);

    IASAbstractStringBuilderable append(char[] str);

    IASAbstractStringBuilderable append(boolean b);

    IASAbstractStringBuilderable append(int i);

    IASAbstractStringBuilderable append(char c);

    IASAbstractStringBuilderable append(long lng);

    IASAbstractStringBuilderable append(float f);

    IASAbstractStringBuilderable append(double d);

    IASAbstractStringBuilderable append(CharSequence charSequence);

    IASAbstractStringBuilderable append(CharSequence charSequence, int start, int end);

    IASAbstractStringBuilderable appendCodePoint(int codePoint);

    IASAbstractStringBuilderable delete(int start, int end);

    IASAbstractStringBuilderable deleteCharAt(int index);

    IASAbstractStringBuilderable replace(int start, int end, IASStringable str);

    IASAbstractStringBuilderable insert(int index, char[] str, int offset,
                                        int len);

    IASAbstractStringBuilderable insert(int offset, Object obj);

    IASAbstractStringBuilderable insert(int offset, IASStringable str);

    IASAbstractStringBuilderable insert(int offset, char[] str);

    IASAbstractStringBuilderable insert(int dstOffset, CharSequence s);

    IASAbstractStringBuilderable insert(int dstOffset, CharSequence s,
                                        int start, int end);

    IASAbstractStringBuilderable insert(int offset, boolean b);

    IASAbstractStringBuilderable insert(int offset, char c);

    IASAbstractStringBuilderable insert(int offset, int i);

    IASAbstractStringBuilderable insert(int offset, long l);

    IASAbstractStringBuilderable insert(int offset, float f);

    IASAbstractStringBuilderable insert(int offset, double d);

    int indexOf(IASStringable str);

    int indexOf(IASStringable str, int fromIndex);

    int lastIndexOf(IASStringable str);

    int lastIndexOf(IASStringable str, int fromIndex);

    IASAbstractStringBuilderable reverse();

    IASStringable toIASString();

    int capacity();

    IASStringable substring(int start);

    IASStringable substring(int start, int end);

    void setCharAt(int index, char c);

    void ensureCapacity(int minimumCapacity);

    void trimToSize();

    StringBuilder getStringBuilder();

    default StringBuffer getStringBuffer() {
        return new StringBuffer(getStringBuilder());
    }

    void setLength(int newLength);

    void setTaint(IASTaintSource source);
}
