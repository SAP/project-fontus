package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintInformation;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintRange;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SuppressWarnings("ALL")
public final class IASString implements IASTaintAware, Comparable<IASString>, CharSequence {

    private String str;
    private IASTaintInformation taintInformation = new IASTaintInformation();

    public IASString() {
        this.str = "";
    }

    public IASString(String s) {
        this.str = s;
    }

    public IASString(String s, boolean tainted) {
        this.str = s;
        this.taintInformation.addRange(0, s.length() - 1, (short) -1);
    }

    public IASString(String s, List<IASTaintRange> ranges) {
        this.str = s;
        this.taintInformation.appendRanges(ranges);
    }

    public IASString(CharSequence sequence, List<IASTaintRange> ranges) {
        this.str = sequence.toString();
        this.taintInformation.appendRanges(ranges);
    }

    public static IASString tainted(String str) {
        return new IASString(str, true);
    }

    @Override
    public boolean isTainted() {
        return this.taintInformation.isTainted();
    }

    /**
     * Marks the whole string as tainted.
     * If the string already has tainted parts, they will be removed and replaced with one range tainting all.
     *
     * @param taint
     */
    @Override
    public void setTaint(boolean taint) {
        if (isTainted()) {
            this.taintInformation.removeAll();
        }
        if (taint) {
            this.taintInformation.addRange(0, this.str.length(), (short) -1);
        }
    }

    public IASString(char value[]) {
        this.str = new String(value);
    }

    public IASString(char value[], int offset, int count) {
        this.str = new String(value, offset, count);
    }

    public IASString(int[] codePoints, int offset, int count) {
        this.str = new String(codePoints, offset, count);
    }

    public IASString(byte ascii[], int hibyte, int offset, int count) {
        this.str = new String(ascii, hibyte, offset, count);
    }

    public IASString(byte ascii[], int hibyte) {
        this.str = new String(ascii, hibyte);
    }

    public IASString(byte bytes[], int offset, int length, String charsetName)
            throws UnsupportedEncodingException {
        this.str = new String(bytes, offset, length, charsetName);
    }

    public IASString(byte bytes[], int offset, int length, Charset charset) {
        this.str = new String(bytes, offset, length, charset);
    }

    public IASString(byte bytes[], String charsetName) throws UnsupportedEncodingException {
        this.str = new String(bytes, charsetName);
    }

    public IASString(byte bytes[], Charset charset) {
        this.str = new String(bytes, charset);
    }

    public IASString(byte bytes[], int offset, int length) {
        this.str = new String(bytes, offset, length);
    }

    public IASString(byte[] bytes) {
        this.str = new String(bytes);
    }

    public IASString(StringBuffer buffer) {
        this.str = new String(buffer);
    }

    public IASString(IASStringBuilder builder) {
        this.str = builder.toString();
        this.taintInformation.appendRangesFrom(builder.getTaintInformation());
    }

    public IASString(IASStringBuffer buffer) {
        this.str = buffer.toString();
        this.taintInformation.appendRangesFrom(buffer.getTaintInformation());
    }

    public IASString(IASString string) {
        this.str = string.str;
        this.taintInformation.appendRangesFrom(string.taintInformation);
    }

    /**
     * Creates a new taintable String from a charsequence.
     * If it's marked as tainted, the whole string will be marked as tainted
     *
     * @param cs
     * @param tainted
     */
    private IASString(CharSequence cs, boolean tainted) {
        this.str = cs.toString();
        this.setTaint(tainted);
    }

    public int length() {
        return this.str.length();
    }

    public boolean isEmpty() {
        return this.str.isEmpty();
    }

    public char charAt(int index) {
        return this.str.charAt(index);
    }

    public int codePointAt(int index) {
        return this.str.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return this.str.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return this.str.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return this.str.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        this.str.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
        this.str.getBytes(srcBegin, srcEnd, dst, dstBegin);
    }

    public byte[] getBytes(IASString charsetName) throws UnsupportedEncodingException {
        return this.str.getBytes(charsetName.str);
    }

    public byte[] getBytes(Charset charset) {
        return this.str.getBytes(charset);
    }

    public byte[] getBytes() {
        return this.str.getBytes();
    }

    public boolean equals(Object anObject) {
        if (!(anObject instanceof IASString)) return false;
        IASString other = (IASString) anObject;
        return this.str.equals(other.str);
    }

    public boolean contentEquals(IASStringBuffer sb) {
        return this.str.contentEquals(sb.getBuffer());
    }

    public boolean contentEquals(StringBuffer sb) {
        return this.str.contentEquals(sb);
    }

    public boolean contentEquals(CharSequence cs) {
        return this.str.contentEquals(cs);
    }

    public boolean equalsIgnoreCase(IASString anotherString) {
        return this.str.equalsIgnoreCase(anotherString.str);
    }

    @Override
    public int compareTo(IASString anotherString) {
        return this.str.compareTo(anotherString.str);
    }

    public int compareToIgnoreCase(IASString str) {
        return this.str.compareToIgnoreCase(str.str);
    }

    public boolean regionMatches(int toffset, IASString other, int ooffset, int len) {
        return this.str.regionMatches(toffset, other.str, ooffset, len);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, IASString other, int ooffset, int len) {
        return this.str.regionMatches(ignoreCase, toffset, other.str, ooffset, len);
    }

    public boolean startsWith(IASString prefix, int toffset) {
        return this.str.startsWith(prefix.str, toffset);
    }

    public boolean startsWith(IASString prefix) {
        return this.str.startsWith(prefix.str);
    }

    public boolean endsWith(IASString suffix) {
        return this.str.endsWith(suffix.str);
    }

    //TODO: sound?
    public int hashCode() {
        return this.str.hashCode();
    }

    public int indexOf(int ch) {
        return this.str.indexOf(ch);
    }

    public int indexOf(int ch, int fromIndex) {
        return this.str.indexOf(ch, fromIndex);
    }

    public int lastIndexOf(int ch) {
        return this.str.lastIndexOf(ch);
    }

    public int lastIndexOf(int ch, int fromIndex) {
        return this.str.lastIndexOf(ch, fromIndex);
    }

    public int indexOf(IASString str) {
        return this.str.indexOf(str.str);
    }

    public int indexOf(IASString str, int fromIndex) {
        return this.str.indexOf(str.str, fromIndex);
    }

    public int lastIndexOf(IASString str) {
        return this.str.lastIndexOf(str.str);
    }

    public int lastIndexOf(IASString str, int fromIndex) {
        return this.str.lastIndexOf(str.str, fromIndex);
    }

    private List<IASTaintRange> getSubstringRanges(int beginIndex, int endIndex) {
        List<IASTaintRange> ranges = this.taintInformation.getRanges(beginIndex, endIndex);
        IASTaintRangeUtils.adjustRanges(ranges, beginIndex, endIndex, beginIndex);
        return ranges;
    }

    public IASString substring(int beginIndex) {
        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, this.length());
        return new IASString(this.str.substring(beginIndex), ranges);
    }

    public IASString substring(int beginIndex, int endIndex) {
        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, endIndex);
        return new IASString(this.str.substring(beginIndex, endIndex), ranges);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, endIndex);
        return new IASString(this.str.subSequence(beginIndex, endIndex), ranges);
    }

    public IASString concat(IASString str) {
        List<IASTaintRange> ranges = this.taintInformation.getAllRanges();
        IASString newStr = new IASString(this.str.concat(str.str), this.taintInformation.getAllRanges());

        List<IASTaintRange> otherRanges = str.taintInformation.getAllRanges();
        IASTaintRangeUtils.adjustRanges(otherRanges, 0, str.length(), -this.length());

        newStr.taintInformation.appendRanges(otherRanges);
        return newStr;
    }

    /**
     * Same behaviour like {@link String#replace(char, char)}
     * The new string gets the same taint ranges as the original one
     *
     * @param oldChar
     * @param newChar
     * @return
     */
    public IASString replace(char oldChar, char newChar) {
        return new IASString(this.str.replace(oldChar, newChar), this.taintInformation.getAllRanges());
    }

    public boolean matches(IASString regex) {
        return this.str.matches(regex.str);
    }

    public boolean contains(CharSequence s) {
        return this.str.contains(s);
    }

    public IASString replaceFirst(IASString regex, IASString replacement) {
        String replacedStr = this.str.replaceFirst(regex.str, replacement.str);
        IASString newStr = new IASString(replacedStr, this.taintInformation.getAllRanges());

        // Are there any changes through the replacement? If not, it's irrelevant if one happened for the tainting
        if (!replacedStr.equals(this.str)) {
            Pattern p = Pattern.compile(regex.str);
            Matcher m = p.matcher(this.str);

            if (m.find()) {
                final int start = m.start();
                final int end = m.end();

                newStr.taintInformation.replaceTaintInformation(start, end, replacement.taintInformation.getAllRanges(), replacement.length());
            }
        }
        return new IASStringTaintRange(this.str.replaceFirst(regex.str, replacement.str), taint);
    }

    public IASStringTaintRange replaceAll(IASStringTaintRange regex, IASStringTaintRange replacement) {
        String replacedStr = this.str.replaceFirst(regex.str, replacement.str);
        IASStringTaintRange newStr = new IASStringTaintRange(replacedStr, this.taintInformation.getAllRanges());

        // Are there any changes through the replacement? If not, it's irrelevant if one happened for the tainting
        if (!replacedStr.equals(this.str)) {
            Pattern p = Pattern.compile(regex.str);
            Matcher m = p.matcher(this.str);

            for (int i = 0; i < m.groupCount(); i++) {
                final int start = m.start(i);
                final int end = m.end(i);

                newStr.taintInformation.replaceTaintInformation(start, end, replacement.taintInformation.getAllRanges());
            }
        }
        return newStr;
    }

    public IASString replace(CharSequence target, CharSequence replacement) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASString regex, int limit) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASString regex) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString join(CharSequence delimiter, CharSequence... elements) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public static IASString join(CharSequence delimiter,
                                 Iterable<? extends CharSequence> elements) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString toLowerCase(Locale locale) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString toLowerCase() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString toUpperCase(Locale locale) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString toUpperCase() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString trim() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    /* JDK 11 BEGIN */
    public IASString strip() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString stripLeading() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString stripTrailing() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public boolean isBlank() {
        return this.str.isBlank();
    }

    public Stream<IASString> lines() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString repeat(int count) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }
    /* JDK 11 END */

    //TODO: sound?
    public String toString() {
        return this.str.toString();
    }

    public IASString toIASString() {
        return this;
    }

    public IntStream chars() {
        return this.str.chars();
    }

    public IntStream codePoints() {
        return this.str.codePoints();
    }

    public char[] toCharArray() {
        return this.str.toCharArray();
    }

    private static boolean isTainted(Object[] args) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    //TODO: sound?
    public static IASString format(IASString format, Object... args) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    //TODO: sound?
    public static IASString format(Locale l, IASString format, Object... args) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString valueOf(Object obj) {
        if (obj instanceof IASString) {
            return (IASString) obj;
        } else {
            return new IASString(String.valueOf(obj));
        }
    }

    public static IASString valueOf(char data[]) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString valueOf(char data[], int offset, int count) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString copyValueOf(char data[], int offset, int count) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString copyValueOf(char data[]) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString valueOf(boolean b) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString valueOf(char c) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString valueOf(int i) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString valueOf(long l) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString valueOf(float f) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static IASString valueOf(double d) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    //TODO: sound?
    public IASString intern() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }


    public static IASString fromString(String str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public static String asString(IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public String getString() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }
}
