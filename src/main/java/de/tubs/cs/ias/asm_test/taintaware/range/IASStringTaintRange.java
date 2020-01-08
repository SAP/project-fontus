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
public final class IASStringTaintRange implements IASTaintAware, Comparable<IASStringTaintRange>, CharSequence {

    private String str;
    private IASTaintInformation taintInformation = new IASTaintInformation();

    public IASStringTaintRange() {
        this.str = "";
    }

    public IASStringTaintRange(String s) {
        this.str = s;
    }

    public IASStringTaintRange(String s, boolean tainted) {
        this.str = s;
        this.taintInformation.addRange(0, s.length() - 1, (short) -1);
    }

    public IASStringTaintRange(String s, List<IASTaintRange> ranges) {
        this.str = s;
        this.taintInformation.appendRanges(ranges);
    }

    public IASStringTaintRange(CharSequence sequence, List<IASTaintRange> ranges) {
        this.str = sequence.toString();
        this.taintInformation.appendRanges(ranges);
    }

    public static IASStringTaintRange tainted(String str) {
        return new IASStringTaintRange(str, true);
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

    public IASStringTaintRange(char value[]) {
        this.str = new String(value);
    }

    public IASStringTaintRange(char value[], int offset, int count) {
        this.str = new String(value, offset, count);
    }

    public IASStringTaintRange(int[] codePoints, int offset, int count) {
        this.str = new String(codePoints, offset, count);
    }

    public IASStringTaintRange(byte ascii[], int hibyte, int offset, int count) {
        this.str = new String(ascii, hibyte, offset, count);
    }

    public IASStringTaintRange(byte ascii[], int hibyte) {
        this.str = new String(ascii, hibyte);
    }

    public IASStringTaintRange(byte bytes[], int offset, int length, String charsetName)
            throws UnsupportedEncodingException {
        this.str = new String(bytes, offset, length, charsetName);
    }

    public IASStringTaintRange(byte bytes[], int offset, int length, Charset charset) {
        this.str = new String(bytes, offset, length, charset);
    }

    public IASStringTaintRange(byte bytes[], String charsetName) throws UnsupportedEncodingException {
        this.str = new String(bytes, charsetName);
    }

    public IASStringTaintRange(byte bytes[], Charset charset) {
        this.str = new String(bytes, charset);
    }

    public IASStringTaintRange(byte bytes[], int offset, int length) {
        this.str = new String(bytes, offset, length);
    }

    public IASStringTaintRange(byte[] bytes) {
        this.str = new String(bytes);
    }

    public IASStringTaintRange(StringBuffer buffer) {
        this.str = new String(buffer);
    }

    public IASStringTaintRange(IASStringBuilderTaintRange builder) {
        this.str = builder.toString();
        this.taintInformation.appendRangesFrom(builder.getTaintInformation());
    }

    public IASStringTaintRange(IASStringBufferTaintRange buffer) {
        this.str = buffer.toString();
        this.taintInformation.appendRangesFrom(buffer.getTaintInformation());
    }

    public IASStringTaintRange(IASStringTaintRange string) {
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
    private IASStringTaintRange(CharSequence cs, boolean tainted) {
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

    public byte[] getBytes(IASStringTaintRange charsetName) throws UnsupportedEncodingException {
        return this.str.getBytes(charsetName.str);
    }

    public byte[] getBytes(Charset charset) {
        return this.str.getBytes(charset);
    }

    public byte[] getBytes() {
        return this.str.getBytes();
    }

    public boolean equals(Object anObject) {
        if (!(anObject instanceof IASStringTaintRange)) return false;
        IASStringTaintRange other = (IASStringTaintRange) anObject;
        return this.str.equals(other.str);
    }

    public boolean contentEquals(IASStringBufferTaintRange sb) {
        return this.str.contentEquals(sb.getBuffer());
    }

    public boolean contentEquals(StringBuffer sb) {
        return this.str.contentEquals(sb);
    }

    public boolean contentEquals(CharSequence cs) {
        return this.str.contentEquals(cs);
    }

    public boolean equalsIgnoreCase(IASStringTaintRange anotherString) {
        return this.str.equalsIgnoreCase(anotherString.str);
    }

    @Override
    public int compareTo(IASStringTaintRange anotherString) {
        return this.str.compareTo(anotherString.str);
    }

    public int compareToIgnoreCase(IASStringTaintRange str) {
        return this.str.compareToIgnoreCase(str.str);
    }

    public boolean regionMatches(int toffset, IASStringTaintRange other, int ooffset, int len) {
        return this.str.regionMatches(toffset, other.str, ooffset, len);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, IASStringTaintRange other, int ooffset, int len) {
        return this.str.regionMatches(ignoreCase, toffset, other.str, ooffset, len);
    }

    public boolean startsWith(IASStringTaintRange prefix, int toffset) {
        return this.str.startsWith(prefix.str, toffset);
    }

    public boolean startsWith(IASStringTaintRange prefix) {
        return this.str.startsWith(prefix.str);
    }

    public boolean endsWith(IASStringTaintRange suffix) {
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

    public int indexOf(IASStringTaintRange str) {
        return this.str.indexOf(str.str);
    }

    public int indexOf(IASStringTaintRange str, int fromIndex) {
        return this.str.indexOf(str.str, fromIndex);
    }

    public int lastIndexOf(IASStringTaintRange str) {
        return this.str.lastIndexOf(str.str);
    }

    public int lastIndexOf(IASStringTaintRange str, int fromIndex) {
        return this.str.lastIndexOf(str.str, fromIndex);
    }

    private List<IASTaintRange> getSubstringRanges(int beginIndex, int endIndex) {
        List<IASTaintRange> ranges = this.taintInformation.getRanges(beginIndex, endIndex);
        IASTaintInformation.adjustRanges(ranges, beginIndex, endIndex, beginIndex);
        return ranges;
    }

    public IASStringTaintRange substring(int beginIndex) {
        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, this.length());
        return new IASStringTaintRange(this.str.substring(beginIndex), ranges);
    }
    
    public IASStringTaintRange substring(int beginIndex, int endIndex) {
        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, endIndex);
        return new IASStringTaintRange(this.str.substring(beginIndex, endIndex), ranges);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, endIndex);
        return new IASStringTaintRange(this.str.subSequence(beginIndex, endIndex), ranges);
    }

    public IASStringTaintRange concat(IASStringTaintRange str) {
        List<IASTaintRange> ranges = this.taintInformation.getAllRanges();
        IASStringTaintRange newStr = new IASStringTaintRange(this.str.concat(str.str), this.taintInformation.getAllRanges());

        List<IASTaintRange> otherRanges = str.taintInformation.getAllRanges();
        IASTaintInformation.adjustRanges(otherRanges, 0, str.length(), -this.length());

        newStr.taintInformation.appendRanges(otherRanges);
        return newStr;
    }

    /**
     * Same behaviour like {@link String#replace(char, char)}
     * The new string gets the same taint ranges as the original one
     * @param oldChar
     * @param newChar
     * @return
     */
    public IASStringTaintRange replace(char oldChar, char newChar) {
        return new IASStringTaintRange(this.str.replace(oldChar, newChar), this.taintInformation.getAllRanges());
    }

    public boolean matches(IASStringTaintRange regex) {
        return this.str.matches(regex.str);
    }

    public boolean contains(CharSequence s) {
        return this.str.contains(s);
    }

    public IASStringTaintRange replaceFirst(IASStringTaintRange regex, IASStringTaintRange replacement) {
        String newStr = this.str.replaceFirst(regex.str, replacement.str);
        // TODO: this seems pretty expensive..
        boolean taint = this.tainted;
        Pattern p = Pattern.compile(regex.str);
        Matcher m = p.matcher(this.str);
        if (m.find()) {
            taint |= replacement.tainted;
        }
        return new IASStringTaintRange(this.str.replaceFirst(regex.str, replacement.str), taint);
    }

    public IASStringTaintRange replaceAll(IASStringTaintRange regex, IASStringTaintRange replacement) {
        // TODO: this seems pretty expensive..
        boolean taint = this.tainted;
        Pattern p = Pattern.compile(regex.str);
        Matcher m = p.matcher(this.str);
        if (m.find()) {
            taint |= replacement.tainted;
        }
        return new IASStringTaintRange(this.str.replaceAll(regex.str, replacement.str), taint);
    }

    public IASStringTaintRange replace(CharSequence target, CharSequence replacement) {
        boolean taint = this.tainted;
        if (this.str.contains(target)) {
            if (replacement instanceof IASTaintAware) {
                IASTaintAware t = (IASTaintAware) replacement;
                taint |= t.isTainted();
            }
        }
        return new IASStringTaintRange(this.str.replace(target, replacement), taint);
    }

    // TODO: this propagates the taint for the whole string
    public IASStringTaintRange[] split(IASStringTaintRange regex, int limit) {
        String[] split = this.str.split(regex.str, limit);
        IASStringTaintRange[] splitted = new IASStringTaintRange[split.length];
        for (int i = 0; i < split.length; i++) {
            splitted[i] = new IASStringTaintRange(split[i], this.tainted);
        }
        return splitted;
    }

    // TODO: this propagates the taint for the whole string
    public IASStringTaintRange[] split(IASStringTaintRange regex) {
        String[] split = this.str.split(regex.str);
        IASStringTaintRange[] splitted = new IASStringTaintRange[split.length];
        for (int i = 0; i < split.length; i++) {
            splitted[i] = new IASStringTaintRange(split[i], this.tainted);
        }
        return splitted;
    }

    public static IASStringTaintRange join(CharSequence delimiter, CharSequence... elements) {
        boolean taint = false;
        for (CharSequence cs : elements) {
            if (cs instanceof IASTaintAware) {
                IASTaintAware t = (IASTaintAware) cs;
                taint |= t.isTainted();
            }
        }
        // Don't forget the delimiter!
        if (delimiter instanceof IASTaintAware) {
            IASTaintAware t = (IASTaintAware) delimiter;
            taint |= t.isTainted();
        }
        return new IASStringTaintRange(String.join(delimiter, elements), taint);
    }


    public static IASStringTaintRange join(CharSequence delimiter,
                                           Iterable<? extends CharSequence> elements) {
        boolean taint = false;
        for (CharSequence cs : elements) {
            if (cs instanceof IASTaintAware) {
                IASTaintAware t = (IASTaintAware) cs;
                taint |= t.isTainted();
            }
        }
        // Don't forget the delimiter!
        if (delimiter instanceof IASTaintAware) {
            IASTaintAware t = (IASTaintAware) delimiter;
            taint |= t.isTainted();
        }
        return new IASStringTaintRange(String.join(delimiter, elements), taint);
    }

    public IASStringTaintRange toLowerCase(Locale locale) {
        return new IASStringTaintRange(this.str.toLowerCase(locale), this.tainted);
    }

    public IASStringTaintRange toLowerCase() {
        return new IASStringTaintRange(this.str.toLowerCase(), this.tainted);
    }

    public IASStringTaintRange toUpperCase(Locale locale) {
        return new IASStringTaintRange(this.str.toUpperCase(locale), this.tainted);
    }

    public IASStringTaintRange toUpperCase() {
        return new IASStringTaintRange(this.str.toUpperCase(), this.tainted);
    }

    public IASStringTaintRange trim() {
        String trimmed = this.str.trim();
        if (trimmed.isEmpty()) {
            return new IASStringTaintRange("");
        }
        return new IASStringTaintRange(trimmed, this.tainted);
    }

    /* JDK 11 BEGIN */
    public IASStringTaintRange strip() {
        String stripped = this.str.strip();
        if (stripped.isEmpty()) {
            return new IASStringTaintRange("");
        }
        return new IASStringTaintRange(stripped, this.tainted);
    }

    public IASStringTaintRange stripLeading() {
        String stripped = this.str.stripLeading();
        if (stripped.isEmpty()) {
            return new IASStringTaintRange("");
        }
        return new IASStringTaintRange(stripped, this.tainted);
    }

    public IASStringTaintRange stripTrailing() {
        String stripped = this.str.stripTrailing();
        if (stripped.isEmpty()) {
            return new IASStringTaintRange("");
        }
        return new IASStringTaintRange(stripped, this.tainted);
    }

    public boolean isBlank() {
        return this.str.isBlank();
    }

    public Stream<IASStringTaintRange> lines() {
        return this.str.lines().map(s -> new IASStringTaintRange(s, this.tainted));
    }

    public IASStringTaintRange repeat(int count) {
        return new IASStringTaintRange(this.str.repeat(count), this.tainted);
    }
    /* JDK 11 END */

    //TODO: sound?
    public String toString() {
        return this.str.toString();
    }

    public IASStringTaintRange toIASString() {
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
        boolean tainted = false;
        for (Object obj : args) {
            if (obj instanceof IASTaintAware) {
                IASTaintAware ta = (IASTaintAware) obj;
                tainted |= ta.isTainted();
            }
        }
        return tainted;
    }

    //TODO: sound?
    public static IASStringTaintRange format(IASStringTaintRange format, Object... args) {
        return new IASStringTaintRange(String.format(format.str, args), isTainted(args));
    }


    //TODO: sound?
    public static IASStringTaintRange format(Locale l, IASStringTaintRange format, Object... args) {
        return new IASStringTaintRange(String.format(l, format.str, args), isTainted(args));
    }

    public static IASStringTaintRange valueOf(Object obj) {
        if (obj instanceof IASStringTaintRange) {
            return (IASStringTaintRange) obj;
        } else {
            return new IASStringTaintRange(String.valueOf(obj));
        }
    }

    public static IASStringTaintRange valueOf(char data[]) {
        return new IASStringTaintRange(String.valueOf(data));
    }

    public static IASStringTaintRange valueOf(char data[], int offset, int count) {
        return new IASStringTaintRange(String.valueOf(data, offset, count));
    }

    public static IASStringTaintRange copyValueOf(char data[], int offset, int count) {
        return new IASStringTaintRange(String.copyValueOf(data, offset, count));
    }

    public static IASStringTaintRange copyValueOf(char data[]) {
        return new IASStringTaintRange(String.copyValueOf(data));
    }

    public static IASStringTaintRange valueOf(boolean b) {
        return new IASStringTaintRange(String.valueOf(b));
    }

    public static IASStringTaintRange valueOf(char c) {
        return new IASStringTaintRange(String.valueOf(c));
    }

    public static IASStringTaintRange valueOf(int i) {
        return new IASStringTaintRange(String.valueOf(i));
    }

    public static IASStringTaintRange valueOf(long l) {
        return new IASStringTaintRange(String.valueOf(l));
    }

    public static IASStringTaintRange valueOf(float f) {
        return new IASStringTaintRange(String.valueOf(f));
    }

    public static IASStringTaintRange valueOf(double d) {
        return new IASStringTaintRange(String.valueOf(d));
    }

    //TODO: sound?
    public IASStringTaintRange intern() {
        return new IASStringTaintRange(this.str.intern(), this.tainted);
    }


    public static IASStringTaintRange fromString(String str) {
        if (str == null) return null;

        return new IASStringTaintRange(str);
    }

    public static String asString(IASStringTaintRange str) {
        if (str == null) return null;
        return str.getString();
    }

    public String getString() {
        return this.str;
    }
}
