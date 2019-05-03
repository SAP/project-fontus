import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Spliterator;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;



public class IASString {
    private String str;

    public IASString(String s) {
        this.str = s;
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
        //TODO: fixme
        return this.str.equals(anObject);
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

    public IASString substring(int beginIndex) {
        return new IASString(this.str.substring(beginIndex));
    }

    public IASString substring(int beginIndex, int endIndex) {
        return new IASString(this.str.substring(beginIndex, endIndex));
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return this.str.subSequence(beginIndex, endIndex);
    }

    public IASString concat(IASString str) {
        return new IASString(this.str.concat(str.str));
    }

    public IASString replace(char oldChar, char newChar) {
        return new IASString(this.str.replace(oldChar, newChar));
    }

    public boolean matches(IASString regex) {
        return this.str.matches(regex.str);
    }

    public boolean contains(CharSequence s) {
        return this.str.contains(s);
    }

    public IASString replaceFirst(IASString regex, IASString replacement) {
        return new IASString(this.str.replaceFirst(regex.str, replacement.str));
    }

    public IASString replaceAll(IASString regex, IASString replacement) {
        return new IASString(this.str.replaceAll(regex.str, replacement.str));
    }

    public IASString replace(CharSequence target, CharSequence replacement) {
        return new IASString(this.str.replace(target, replacement));
    }

    public IASString[] split(IASString regex, int limit) {
        String[] split = this.str.split(regex.str, limit);
        IASString[] splitted = new IASString[split.length];
        for(int i=0;i<split.length;i++) {
            splitted[i] = new IASString(split[i]);
        }
        return splitted;
    }

    public IASString[] split(IASString regex) {

        String[] split = this.str.split(regex.str);
        IASString[] splitted = new IASString[split.length];
        for(int i=0;i<split.length;i++) {
            splitted[i] = new IASString(split[i]);
        }
        return splitted;
    }

    public static IASString join(CharSequence delimiter, CharSequence... elements) {
        return new IASString(String.join(delimiter, elements));
    }


    public static IASString join(CharSequence delimiter,
            Iterable<? extends CharSequence> elements) {
        return new IASString(String.join(delimiter, elements));
    }

    public IASString toLowerCase(Locale locale) {
        return new IASString(this.str.toLowerCase(locale));
    }

    public IASString toLowerCase() {
        return new IASString(this.str.toLowerCase());
    }

    public IASString toUpperCase(Locale locale) {
        return new IASString(this.str.toUpperCase(locale));
    }

    public IASString toUpperCase() {
        return new IASString(this.str.toUpperCase());
    }

    public IASString trim() {
        return new IASString(this.str.trim());
    }

    /* JDK 11
    public String strip() {
        return this.str.strip();
    }

    public String stripLeading() {
        return this.str.stripLeading();
    }

    public String stripTrailing() {
        return this.str.stripTrailing();
    }

    public boolean isBlank() {
        return this.str.isBlank();
    }

    public Stream<String> lines() {
        return this.str.lines();
    }
    */

    //TODO: sound?
    public String toString() {
        return this.str.toString();
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


    //TODO: sound?
    public static IASString format(IASString format, Object... args) {
        return new IASString(String.format(format.str, args));
    }


    //TODO: sound?
    public static IASString format(Locale l, IASString format, Object... args) {
        return new IASString(String.format(l, format.str, args));
    }

    public static IASString valueOf(Object obj) {
        return new IASString(String.valueOf(obj));
    }

    public static IASString valueOf(char data[]) {
        return new IASString(String.valueOf(data));
    }

    public static IASString valueOf(char data[], int offset, int count) {
        return new IASString(String.valueOf(data, offset, count));
    }

    public static IASString copyValueOf(char data[], int offset, int count) {
        return new IASString(String.copyValueOf(data, offset, count));
    }

    public static IASString copyValueOf(char data[]) {
        return new IASString(String.copyValueOf(data));
    }

    public static IASString valueOf(boolean b) {
        return new IASString(String.valueOf(b));
    }

    public static IASString valueOf(char c) {
        return new IASString(String.valueOf(c));
    }

    public static IASString valueOf(int i) {
        return new IASString(String.valueOf(i));
    }

    public static IASString valueOf(long l) {
        return new IASString(String.valueOf(l));
    }

    public static IASString valueOf(float f) {
        return new IASString(String.valueOf(f));
    }
    public static IASString valueOf(double d) {
        return new IASString(String.valueOf(d));
    }

    //TODO: sound?
    public String intern() {
        return this.str.intern();
    }

    /* JDK11
    public String repeat(int count) {
        return this.str.repeat(count);
    }
    */


    public static IASString concat(IASString lhs, IASString rhs) {
        return new IASString(lhs.str + rhs.str);
    }

    public static IASString concat(IASString lhs, int rhs) {
        return new IASString(lhs.str + rhs);
    }

    public static IASString concat(IASString lhs, long rhs) {
        return new IASString(lhs.str + rhs);
    }

    public static IASString concat(IASString lhs, float rhs) {
        return new IASString(lhs.str + rhs);
    }
    public static IASString concat(IASString lhs, double rhs) {
        return new IASString(lhs.str + rhs);
    }

    public String getString() {
        return this.str;
    }
}
