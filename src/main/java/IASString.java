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

    public IASString(StringBuilder builder) {
        this.str = new String(builder);
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

    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        return this.str.getBytes(charsetName);
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

    public boolean equalsIgnoreCase(String anotherString) {
        return this.str.equalsIgnoreCase(anotherString);
    }

    public int compareTo(String anotherString) {
        return this.str.compareTo(anotherString);
    }

    public int compareToIgnoreCase(String str) {
        return this.str.compareToIgnoreCase(str);
    }

    public boolean regionMatches(int toffset, String other, int ooffset, int len) {
        return this.str.regionMatches(toffset, other, ooffset, len);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        return this.str.regionMatches(ignoreCase, toffset, other, ooffset, len);
    }

    public boolean startsWith(String prefix, int toffset) {
        return this.str.startsWith(prefix, toffset);
    }

    public boolean startsWith(String prefix) {
        return this.str.startsWith(prefix);
    }

    public boolean endsWith(String suffix) {
        return this.str.endsWith(suffix);
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

    public int indexOf(String str) {
        return this.str.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return this.str.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return this.str.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return this.str.lastIndexOf(str, fromIndex);
    }

    //TODO: returns string
    public String substring(int beginIndex) {
        return this.str.substring(beginIndex);
    }

    //TODO: returns string
    public String substring(int beginIndex, int endIndex) {
        return this.str.substring(beginIndex, endIndex);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return this.subSequence(beginIndex, endIndex);
    }

    //TODO: returns, takes string
    public String concat(String str) {
        return this.str.concat(str);
    }

    //TODO: returns string
    public String replace(char oldChar, char newChar) {
        return this.str.replace(oldChar, newChar);
    }

    //TODO: takes string
    public boolean matches(String regex) {
        return this.str.matches(regex);
    }

    public boolean contains(CharSequence s) {
        return this.str.contains(s);
    }

    //TODO: returns, takes string
    public String replaceFirst(String regex, String replacement) {
        return this.str.replaceFirst(regex, replacement);
    }

    //TODO: returns, takes string
    public String replaceAll(String regex, String replacement) {
        return this.str.replaceAll(regex, replacement);
    }

    //TODO: returns string
    public String replace(CharSequence target, CharSequence replacement) {
        return this.str.replace(target, replacement);
    }

    //TODO: returns, takes string
    public String[] split(String regex, int limit) {
        return this.str.split(regex, limit);
    }

    //TODO: returns, takes string
    public String[] split(String regex) {
        return this.str.split(regex);
    }

    //TODO: returns, takes string
    public static String join(CharSequence delimiter, CharSequence... elements) {
        return String.join(delimiter, elements);
    }


    //TODO: returns, takes string
    public static String join(CharSequence delimiter,
            Iterable<? extends CharSequence> elements) {
        return String.join(delimiter, elements);
    }

    //TODO: returns, takes string
    public String toLowerCase(Locale locale) {
        return this.str.toLowerCase(locale);
    }

    //TODO: returns, takes string
    public String toLowerCase() {
        return this.str.toLowerCase();
    }

    //TODO: returns, takes string
    public String toUpperCase(Locale locale) {
        return this.str.toUpperCase(locale);
    }

    //TODO: returns, takes string
    public String toUpperCase() {
        return this.str.toUpperCase();
    }

    public String trim() {
        return this.str.trim();
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
    public static String format(String format, Object... args) {
        return String.format(format, args);
    }

    //TODO: sound?
    public static String format(Locale l, String format, Object... args) {
        return String.format(l, format, args);
    }

    public static String valueOf(Object obj) {
        return String.valueOf(obj);
    }

    public static String valueOf(char data[]) {
        return String.valueOf(data);
    }

    public static String valueOf(char data[], int offset, int count) {
        return String.valueOf(data, offset, count);
    }

    public static String copyValueOf(char data[], int offset, int count) {
        return String.copyValueOf(data, offset, count);
    }

    public static String copyValueOf(char data[]) {
        return String.copyValueOf(data);
    }

    public static String valueOf(boolean b) {
        return String.valueOf(b);
    }

    public static String valueOf(char c) {
        return String.valueOf(c);
    }

    public static String valueOf(int i) {
        return String.valueOf(i);
    }

    public static String valueOf(long l) {
        return String.valueOf(l);
    }

    public static String valueOf(float f) {
        return String.valueOf(f);
    }
    public static String valueOf(double d) {
        return String.valueOf(d);
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
