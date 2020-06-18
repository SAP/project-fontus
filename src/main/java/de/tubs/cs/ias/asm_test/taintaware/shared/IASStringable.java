package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface IASStringable extends IASTaintAware, Comparable<IASStringable>, CharSequence {
    void abortIfTainted();

    int length();

    boolean isEmpty();

    char charAt(int index);

    int codePointAt(int index);

    int codePointBefore(int index);

    int codePointCount(int beginIndex, int endIndex);

    int offsetByCodePoints(int index, int codePointOffset);

    void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin);

    void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin);

    byte[] getBytes(IASStringable charsetName) throws UnsupportedEncodingException;

    byte[] getBytes(Charset charset);

    byte[] getBytes();

    boolean equals(Object anObject);

    boolean contentEquals(IASStringBuilderable sb);

//    boolean contentEquals(StringBuffer sb);

    boolean contentEquals(CharSequence cs);

    boolean equalsIgnoreCase(IASStringable anotherString);

    int compareToIgnoreCase(IASStringable str);

    boolean regionMatches(int toffset, IASStringable other, int ooffset, int len);

    boolean regionMatches(boolean ignoreCase, int toffset, IASStringable other, int ooffset, int len);

    boolean startsWith(IASStringable prefix, int toffset);

    boolean startsWith(IASStringable prefix);

    boolean endsWith(IASStringable suffix);

    int hashCode();

    int indexOf(int ch);

    int indexOf(int ch, int fromIndex);

    int lastIndexOf(int ch);

    int lastIndexOf(int ch, int fromIndex);

    int indexOf(IASStringable str);

    int indexOf(IASStringable str, int fromIndex);

    int lastIndexOf(IASStringable str);

    int lastIndexOf(IASStringable str, int fromIndex);

    IASStringable substring(int beginIndex);

    IASStringable substring(int beginIndex, int endIndex);

    CharSequence subSequence(int beginIndex, int endIndex);

    IASStringable concat(IASStringable str);

    IASStringable replace(char oldChar, char newChar);

    boolean matches(IASStringable regex);

    boolean contains(CharSequence s);

    IASStringable replaceFirst(IASStringable regex, IASStringable replacement);

    IASStringable replaceAll(IASStringable regex, IASStringable replacement);

    IASStringable replace(CharSequence target, CharSequence replacement);

    IASStringable[] split(IASStringable regex, int limit);

    IASStringable[] split(IASStringable regex);

    IASStringable toLowerCase(Locale locale);

    IASStringable toLowerCase();

    IASStringable toUpperCase(Locale locale);

    IASStringable toUpperCase();

    IASStringable trim();

    /* JDK 11 BEGIN */
    IASStringable strip();

    IASStringable stripLeading();

    IASStringable stripTrailing();

    boolean isBlank();

    IASStringable repeat(int count);

    IASStringable toIASString();

    IntStream chars();

    IntStream codePoints();

    char[] toCharArray();

    IASStringable intern();

    String getString();

    Stream<? extends IASStringable> lines();

    IASTaintSource getTaintFor(int i);

    void setTaint(IASTaintSource source);
}
