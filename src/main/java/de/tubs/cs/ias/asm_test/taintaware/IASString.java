package de.tubs.cs.ias.asm_test.taintaware;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SuppressWarnings("ALL")
public final class IASString implements IASTaintAware, Comparable<IASString>, CharSequence {

    private String str;
    private boolean tainted = false;
    private static ConcurrentHashMap<String, IASString> internPool = new ConcurrentHashMap<>();

    public IASString() {
        this.str = "";
        this.tainted = false;
    }

    public IASString(String s) {
        this.str = s;
        this.tainted = false;
    }

    public IASString(String s, boolean tainted) {
        this.str = s;
        this.tainted = tainted;
    }

    public static IASString tainted(String str) {
        return new IASString(str, true);
    }

    public static IASString tainted(IASString tstr) {
        tstr.tainted = true;
        return tstr;
    }

    @Override
    public boolean isTainted() {
        return this.tainted;
    }

    @Override
    public void setTaint(boolean b) {
        // Prevent tainting of empty strings
        if (str.length() > 0) {
            this.tainted = b;
        }
    }

    private void mergeTaint(IASTaintAware other) {
        this.tainted |= other.isTainted();
    }

    public void abortIfTainted() {
        if (this.tainted) {
            System.err.printf("String %s is tainted!\nAborting..!\n", this.str);
            System.exit(1);
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

    public IASString(byte bytes[], int offset, int length, IASString charsetName)
            throws UnsupportedEncodingException {
        this.str = new String(bytes, offset, length, charsetName.str);
    }

    public IASString(byte bytes[], int offset, int length, Charset charset) {
        // TODO: howto handle this? Does the charset affect tainting?
        this.str = new String(bytes, offset, length, charset);
    }

    public IASString(byte bytes[], IASString charsetName) throws UnsupportedEncodingException {
        // TODO: howto handle this? Does the charset affect tainting?
        this.str = new String(bytes, charsetName.str);
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
        this.tainted = builder.isTainted();
    }

    public IASString(IASStringBuffer buffer) {
        this.str = buffer.toString();
        this.tainted = buffer.isTainted();
    }

    public IASString(IASString string) {
        this.str = string.str;
        this.tainted = string.tainted;
    }

    private IASString(CharSequence cs, boolean tainted) {
        this.str = cs.toString();
        this.tainted = tainted;
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

    public IASString substring(int beginIndex) {
        return new IASString(this.str.substring(beginIndex), this.tainted);
    }

    public IASString substring(int beginIndex, int endIndex) {
        return new IASString(this.str.substring(beginIndex, endIndex), this.tainted);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return new IASString(this.str.subSequence(beginIndex, endIndex), this.tainted);
    }

    public IASString concat(IASString str) {
        return new IASString(this.str.concat(str.str), this.tainted || str.tainted);
    }

    public IASString replace(char oldChar, char newChar) {
        return new IASString(this.str.replace(oldChar, newChar), this.tainted);
    }

    public boolean matches(IASString regex) {
        return this.str.matches(regex.str);
    }

    public boolean contains(CharSequence s) {
        return this.str.contains(s);
    }

    public IASString replaceFirst(IASString regex, IASString replacement) {
        // TODO: this seems pretty expensive..
        boolean taint = this.tainted;
        Pattern p = Pattern.compile(regex.str);
        Matcher m = p.matcher(this.str);
        if (m.find()) {
            taint |= replacement.tainted;
        }
        return new IASString(this.str.replaceFirst(regex.str, replacement.str), taint);
    }

    public IASString replaceAll(IASString regex, IASString replacement) {
        // TODO: this seems pretty expensive..
        boolean taint = this.tainted;
        Pattern p = Pattern.compile(regex.str);
        Matcher m = p.matcher(this.str);
        if (m.find()) {
            taint |= replacement.tainted;
        }
        return new IASString(this.str.replaceAll(regex.str, replacement.str), taint);
    }

    public IASString replace(CharSequence target, CharSequence replacement) {
        boolean taint = this.tainted;
        if (this.str.contains(target)) {
            if (replacement instanceof IASTaintAware) {
                IASTaintAware t = (IASTaintAware) replacement;
                taint |= t.isTainted();
            }
        }
        return new IASString(this.str.replace(target, replacement), taint);
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASString regex, int limit) {
        String[] split = this.str.split(regex.str, limit);
        IASString[] splitted = new IASString[split.length];
        for (int i = 0; i < split.length; i++) {
            splitted[i] = new IASString(split[i], this.tainted);
        }
        return splitted;
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASString regex) {
        String[] split = this.str.split(regex.str);
        IASString[] splitted = new IASString[split.length];
        for (int i = 0; i < split.length; i++) {
            splitted[i] = new IASString(split[i], this.tainted);
        }
        return splitted;
    }

    public static IASString join(CharSequence delimiter, CharSequence... elements) {
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
        return new IASString(String.join(delimiter, elements), taint);
    }


    public static IASString join(CharSequence delimiter,
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
        return new IASString(String.join(delimiter, elements), taint);
    }

    public IASString toLowerCase(Locale locale) {
        return new IASString(this.str.toLowerCase(locale), this.tainted);
    }

    public IASString toLowerCase() {
        return new IASString(this.str.toLowerCase(), this.tainted);
    }

    public IASString toUpperCase(Locale locale) {
        return new IASString(this.str.toUpperCase(locale), this.tainted);
    }

    public IASString toUpperCase() {
        return new IASString(this.str.toUpperCase(), this.tainted);
    }

    public IASString trim() {
        String trimmed = this.str.trim();
        if (trimmed.isEmpty()) {
            return new IASString("");
        }
        return new IASString(trimmed, this.tainted);
    }

    /* JDK 11 BEGIN */
    public IASString strip() {
        String stripped = this.str.strip();
        if (stripped.isEmpty()) {
            return new IASString("");
        }
        return new IASString(stripped, this.tainted);
    }

    public IASString stripLeading() {
        String stripped = this.str.stripLeading();
        if (stripped.isEmpty()) {
            return new IASString("");
        }
        return new IASString(stripped, this.tainted);
    }

    public IASString stripTrailing() {
        String stripped = this.str.stripTrailing();
        if (stripped.isEmpty()) {
            return new IASString("");
        }
        return new IASString(stripped, this.tainted);
    }

    public boolean isBlank() {
        return this.str.isBlank();
    }

    public Stream<IASString> lines() {
        return this.str.lines().map(s -> new IASString(s, this.tainted));
    }

    public IASString repeat(int count) {
        if(count == 0) {
            return new IASString("");
        }
        return new IASString(this.str.repeat(count), this.tainted);
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
    public static IASString format(IASString format, Object... args) {
        return new IASString(String.format(format.str, args), isTainted(args));
    }


    //TODO: sound?
    public static IASString format(Locale l, IASString format, Object... args) {
        return new IASString(String.format(l, format.str, args), isTainted(args));
    }

    public static IASString valueOf(Object obj) {
        if (obj instanceof IASString) {
            return (IASString) obj;
        } else {
            return new IASString(String.valueOf(obj));
        }
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
    public IASString intern() {
        /*if(this.internPool.containsKey(this.str)) {
            return this.internPool.get(this.str);
        } else {
            this.internPool.put(this.str, this);
            return this;
        }*/
        /*IASString current = this.internPool.get(this.str);
        if(current == null) {
            this.internPool.put(this.str, this);
            return this;
        }
        return current;*/
        IASString rv = internPool.putIfAbsent(this.str, this);
        if (rv == null) {
            return internPool.get(this.str);
        } else {
            return rv;
        }
        //return new IASString(this.str.intern(), this.tainted);
    }


    public static IASString fromString(String str) {
        if (str == null) return null;

        return new IASString(str);
    }

    public static String asString(IASString str) {
        if (str == null) return null;
        return str.getString();
    }

    public String getString() {
        return this.str;
    }

    public static final Comparator<IASString> CASE_INSENSITIVE_ORDER
            = new CaseInsensitiveComparator();

    private static class CaseInsensitiveComparator
            implements Comparator<IASString>, java.io.Serializable {
        private static final long serialVersionUID = 8575799808933029326L;

        public int compare(IASString s1, IASString s2) {
            return s1.compareToIgnoreCase(s2);
        }
    }

}
