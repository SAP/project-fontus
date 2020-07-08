package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.io.IOException;
import java.io.Serializable;

public interface IASStringBuilderable extends Serializable, Comparable<IASStringBuilderable>, Appendable, CharSequence, IASTaintAware {
    IASStringBuilderable append(Object obj);

    IASStringBuilderable append(IASStringable str);

    IASStringBuilderable append(IASStringBuilderable str);

    IASStringBuilderable append(char[] s, int offset, int len);

    IASStringBuilderable append(char[] str);

    IASStringBuilderable append(boolean b);

    IASStringBuilderable append(int i);

    IASStringBuilderable append(char c);

    IASStringBuilderable append(long lng);

    IASStringBuilderable append(float f);

    IASStringBuilderable append(double d);

    IASStringBuilderable append(CharSequence charSequence);

    IASStringBuilderable append(CharSequence charSequence, int start, int end);

    IASStringBuilderable appendCodePoint(int codePoint);

    IASStringBuilderable delete(int start, int end);

    IASStringBuilderable deleteCharAt(int index);

    IASStringBuilderable replace(int start, int end, IASStringable str);

    IASStringBuilderable insert(int index, char[] str, int offset,
                                int len);

    IASStringBuilderable insert(int offset, Object obj);

    IASStringBuilderable insert(int offset, IASStringable str);

    IASStringBuilderable insert(int offset, char[] str);

    IASStringBuilderable insert(int dstOffset, CharSequence s);

    IASStringBuilderable insert(int dstOffset, CharSequence s,
                                int start, int end);

    IASStringBuilderable insert(int offset, boolean b);

    IASStringBuilderable insert(int offset, char c);

    IASStringBuilderable insert(int offset, int i);

    IASStringBuilderable insert(int offset, long l);

    IASStringBuilderable insert(int offset, float f);

    IASStringBuilderable insert(int offset, double d);

    int indexOf(IASStringable str);

    int indexOf(IASStringable str, int fromIndex);

    int lastIndexOf(IASStringable str);

    int lastIndexOf(IASStringable str, int fromIndex);

    IASStringBuilderable reverse();

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
