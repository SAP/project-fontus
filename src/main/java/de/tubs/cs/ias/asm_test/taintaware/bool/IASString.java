package de.tubs.cs.ias.asm_test.taintaware.bool;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringPool;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

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
public final class IASString implements IASTaintAware, IASStringable {

    private String string;
    private boolean tainted = false;
    private static ConcurrentHashMap<String, IASString> internPool = new ConcurrentHashMap<>();

    public IASString() {
        this.string = "";
        this.tainted = false;
    }

    public IASString(IASStringable s) {
        this.string = s.getString();
        this.tainted = s.isTainted();
    }

    public IASString(String s) {
        this.string = s;
        this.tainted = false;
    }

    public IASString(IASStringBuilderable strb) {
        this.string = strb.toString();
        this.tainted = strb.isTainted();
    }

    public IASString(String s, boolean tainted) {
        this.string = s;
        this.tainted = tainted;
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
        if (string.length() > 0) {
            this.tainted = b;
        }
    }

    @Override
    public void setTaint(IASTaintSource source) {
        // Prevent tainting of empty strings
        if (string.length() > 0) {
            this.tainted = source != null;
        }
    }

    private void mergeTaint(IASTaintAware other) {
        this.tainted |= other.isTainted();
    }

    @Override
    public void abortIfTainted() {
        if (this.tainted) {
            System.err.printf("String %s is tainted!\nAborting..!\n", this.string);
            System.exit(1);
        }
    }

    public IASString(char value[]) {
        this.string = new String(value);
    }

    public IASString(char value[], int offset, int count) {
        this.string = new String(value, offset, count);
    }

    public IASString(int[] codePoints, int offset, int count) {
        this.string = new String(codePoints, offset, count);
    }

    public IASString(byte ascii[], int hibyte, int offset, int count) {
        this.string = new String(ascii, hibyte, offset, count);
    }

    public IASString(byte ascii[], int hibyte) {
        this.string = new String(ascii, hibyte);
    }

    public IASString(byte bytes[], int offset, int length, IASString charsetName)
            throws UnsupportedEncodingException {
        this.string = new String(bytes, offset, length, charsetName.string);
    }

    public IASString(byte bytes[], int offset, int length, Charset charset) {
        // TODO: howto handle this? Does the charset affect tainting?
        this.string = new String(bytes, offset, length, charset);
    }

    public IASString(byte bytes[], IASStringable charsetName) throws UnsupportedEncodingException {
        // TODO: howto handle this? Does the charset affect tainting?
        this.string = new String(bytes, charsetName.getString());
    }

    public IASString(byte bytes[], Charset charset) {
        this.string = new String(bytes, charset);
    }

    public IASString(byte bytes[], int offset, int length) {
        this.string = new String(bytes, offset, length);
    }

    public IASString(byte[] bytes) {
        this.string = new String(bytes);
    }

    public IASString(StringBuffer buffer) {
        this.string = new String(buffer);
    }

    public IASString(IASStringBuilder builder) {
        this.string = builder.toString();
        this.tainted = builder.isTainted();
    }

    public IASString(IASStringBuffer buffer) {
        this.string = buffer.toString();
        this.tainted = buffer.isTainted();
    }

    public IASString(IASString string) {
        this.string = string.string;
        this.tainted = string.tainted;
    }

    private IASString(CharSequence cs, boolean tainted) {
        this.string = cs.toString();
        this.tainted = tainted;
    }

    @Override
    public int length() {
        return this.string.length();
    }

    @Override
    public boolean isEmpty() {
        return this.string.isEmpty();
    }

    @Override
    public char charAt(int index) {
        return this.string.charAt(index);
    }

    @Override
    public int codePointAt(int index) {
        return this.string.codePointAt(index);
    }

    @Override
    public int codePointBefore(int index) {
        return this.string.codePointBefore(index);
    }

    @Override
    public int codePointCount(int beginIndex, int endIndex) {
        return this.string.codePointCount(beginIndex, endIndex);
    }

    @Override
    public int offsetByCodePoints(int index, int codePointOffset) {
        return this.string.offsetByCodePoints(index, codePointOffset);
    }

    @Override
    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        this.string.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
        this.string.getBytes(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public byte[] getBytes(IASStringable charsetName) throws UnsupportedEncodingException {
        return this.string.getBytes(charsetName.getString());
    }

    @Override
    public byte[] getBytes(Charset charset) {
        return this.string.getBytes(charset);
    }

    @Override
    public byte[] getBytes() {
        return this.string.getBytes();
    }

    @Override
    public boolean equals(Object anObject) {
        if (!(anObject instanceof IASString)) return false;
        IASString other = (IASString) anObject;
        return this.string.equals(other.string);
    }

    @Override
    public boolean contentEquals(IASStringBuilderable sb) {
        return this.string.contentEquals(sb.getStringBuilder());
    }

    public boolean contentEquals(StringBuffer sb) {
        return this.string.contentEquals(sb);
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
        return this.string.contentEquals(cs);
    }

    @Override
    public boolean equalsIgnoreCase(IASStringable anotherString) {
        if(anotherString == null) {
            return false;
        }
        return this.string.equalsIgnoreCase(anotherString.getString());
    }

    @Override
    public int compareTo(IASStringable anotherString) {
        return this.string.compareTo(anotherString.getString());
    }

    @Override
    public int compareToIgnoreCase(IASStringable str) {
        return this.string.compareToIgnoreCase(str.getString());
    }

    @Override
    public boolean regionMatches(int toffset, IASStringable other, int ooffset, int len) {
        return this.string.regionMatches(toffset, other.getString(), ooffset, len);
    }

    @Override
    public boolean regionMatches(boolean ignoreCase, int toffset, IASStringable other, int ooffset, int len) {
        return this.string.regionMatches(ignoreCase, toffset, other.getString(), ooffset, len);
    }

    @Override
    public boolean startsWith(IASStringable prefix, int toffset) {
        return this.string.startsWith(prefix.getString(), toffset);
    }

    @Override
    public boolean startsWith(IASStringable prefix) {
        return this.string.startsWith(prefix.getString());
    }

    @Override
    public boolean endsWith(IASStringable suffix) {
        return this.string.endsWith(suffix.getString());
    }

    //TODO: sound?
    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @Override
    public int indexOf(int ch) {
        return this.string.indexOf(ch);
    }

    @Override
    public int indexOf(int ch, int fromIndex) {
        return this.string.indexOf(ch, fromIndex);
    }

    @Override
    public int lastIndexOf(int ch) {
        return this.string.lastIndexOf(ch);
    }

    @Override
    public int lastIndexOf(int ch, int fromIndex) {
        return this.string.lastIndexOf(ch, fromIndex);
    }

    @Override
    public int indexOf(IASStringable str) {
        return this.string.indexOf(str.getString());
    }

    @Override
    public int indexOf(IASStringable str, int fromIndex) {
        return this.string.indexOf(str.getString(), fromIndex);
    }

    @Override
    public int lastIndexOf(IASStringable str) {
        return this.string.lastIndexOf(str.getString());
    }

    @Override
    public int lastIndexOf(IASStringable str, int fromIndex) {
        return this.string.lastIndexOf(str.getString(), fromIndex);
    }

    @Override
    public IASString substring(int beginIndex) {
        boolean taint = this.tainted;
        if (beginIndex == this.string.length()) {
            taint = false;
        }
        return new IASString(this.string.substring(beginIndex), taint);
    }

    @Override
    public IASString substring(int beginIndex, int endIndex) {
        boolean taint = this.tainted;
        if (beginIndex == endIndex) {
            taint = false;
        }
        return new IASString(this.string.substring(beginIndex, endIndex), taint);
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        boolean taint = this.tainted;
        if (beginIndex == endIndex) {
            taint = false;
        }
        return new IASString(this.string.subSequence(beginIndex, endIndex), taint);
    }

    @Override
    public IASString concat(IASStringable str) {
        return new IASString(this.string.concat(str.getString()), this.tainted || str.isTainted());
    }

    @Override
    public IASString replace(char oldChar, char newChar) {
        return new IASString(this.string.replace(oldChar, newChar), this.tainted);
    }

    @Override
    public boolean matches(IASStringable regex) {
        return this.string.matches(regex.getString());
    }

    @Override
    public boolean contains(CharSequence s) {
        return this.string.contains(s);
    }

    @Override
    public IASString replaceFirst(IASStringable regex, IASStringable replacement) {
        // TODO: this seems pretty expensive..
        boolean taint = this.tainted;
        Pattern p = Pattern.compile(regex.getString());
        Matcher m = p.matcher(this.string);
        if (m.find()) {
            taint |= replacement.isTainted();
        }
        String result = this.string.replaceFirst(regex.getString(), replacement.getString());
        if (result.isEmpty()) {
            taint = false;
        }
        return new IASString(result, taint);
    }

    @Override
    public IASString replaceAll(IASStringable regex, IASStringable replacement) {
        // TODO: this seems pretty expensive..
        boolean taint = this.tainted;
        Pattern p = Pattern.compile(regex.getString());
        Matcher m = p.matcher(this.string);
        if (m.find()) {
            taint |= replacement.isTainted();
        }
        String result = this.string.replaceAll(regex.getString(), replacement.getString());
        if (result.isEmpty()) {
            taint = false;
        }
        return new IASString(result, taint);
    }

    @Override
    public IASString replace(CharSequence target, CharSequence replacement) {
        boolean taint = this.tainted;
        if (this.string.contains(target)) {
            if (replacement instanceof IASTaintAware) {
                IASTaintAware t = (IASTaintAware) replacement;
                taint |= t.isTainted();
            }
        }
        return new IASString(this.string.replace(target, replacement), taint);
    }

    // TODO: this propagates the taint for the whole string
    @Override
    public IASString[] split(IASStringable regex, int limit) {
        String[] split = this.string.split(regex.getString(), limit);
        IASString[] splitted = new IASString[split.length];
        for (int i = 0; i < split.length; i++) {
            splitted[i] = new IASString(split[i], this.tainted);
        }
        return splitted;
    }

    // TODO: this propagates the taint for the whole string
    @Override
    public IASString[] split(IASStringable regex) {
        String[] split = this.string.split(regex.getString());
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

    @Override
    public IASString toLowerCase(Locale locale) {
        return new IASString(this.string.toLowerCase(locale), this.tainted);
    }

    @Override
    public IASString toLowerCase() {
        return new IASString(this.string.toLowerCase(), this.tainted);
    }

    @Override
    public IASString toUpperCase(Locale locale) {
        return new IASString(this.string.toUpperCase(locale), this.tainted);
    }

    @Override
    public IASString toUpperCase() {
        return new IASString(this.string.toUpperCase(), this.tainted);
    }

    @Override
    public IASString trim() {
        String trimmed = this.string.trim();
        if (trimmed.isEmpty()) {
            return new IASString("");
        }
        return new IASString(trimmed, this.tainted);
    }

    /* JDK 11 BEGIN */
    @Override
    public IASString strip() {
        String stripped = this.string.strip();
        if (stripped.isEmpty()) {
            return new IASString("");
        }
        return new IASString(stripped, this.tainted);
    }

    @Override
    public IASString stripLeading() {
        String stripped = this.string.stripLeading();
        if (stripped.isEmpty()) {
            return new IASString("");
        }
        return new IASString(stripped, this.tainted);
    }

    @Override
    public IASString stripTrailing() {
        String stripped = this.string.stripTrailing();
        if (stripped.isEmpty()) {
            return new IASString("");
        }
        return new IASString(stripped, this.tainted);
    }

    @Override
    public boolean isBlank() {
        return this.string.isBlank();
    }

    public Stream<IASString> lines() {
        return this.string.lines().map(s -> new IASString(s, this.tainted));
    }

    @Override
    public IASTaintSource getTaintFor(int position) {
        return this.tainted ? IASTaintSource.TS_CS_UNKNOWN_ORIGIN : null;
    }

    @Override
    public IASString repeat(int count) {
        if (count == 0) {
            return new IASString("");
        }
        return new IASString(this.string.repeat(count), this.tainted);
    }
    /* JDK 11 END */

    //TODO: sound?
    @Override
    public String toString() {
        return this.string.toString();
    }

    @Override
    public IASString toIASString() {
        return this;
    }

    @Override
    public IntStream chars() {
        return this.string.chars();
    }

    @Override
    public IntStream codePoints() {
        return this.string.codePoints();
    }

    @Override
    public char[] toCharArray() {
        return this.string.toCharArray();
    }

    static boolean isTainted(Object[] args) {
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
    public static IASString format(IASStringable format, Object... args) {
        //return new IASString(String.format(format.toString(), args), isTainted(args));
        return new IASFormatter().format(format, args).toIASString();
    }

    //TODO: sound?
    public static IASString format(Locale l, IASStringable format, Object... args) {
        //return new IASString(String.format(l, format.toString(), args), isTainted(args));
        return new IASFormatter(l).format(format, args).toIASString();
    }

    public static IASString valueOf(Object obj) {
        if (obj instanceof IASString) {
            return (IASString) obj;
        } else if (obj instanceof IASStringBuffer) {
            return (IASString) ((IASStringBuffer) obj).toIASString();
        } else if (obj instanceof IASStringBuilder) {
            return (IASString) ((IASStringBuilder) obj).toIASString();
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
    @Override
    public IASString intern() {
        return (IASString) IASStringPool.intern(this);
    }


    public static IASString fromString(String str) {
        if (str == null) return null;

        return new IASString(str);
    }

    public static String asString(IASString str) {
        if (str == null) return null;
        return str.getString();
    }

    @Override
    public String getString() {
        return this.string;
    }

    public static final Comparator<IASString> CASE_INSENSITIVE_ORDER
            = new CaseInsensitiveComparator();

    private static class CaseInsensitiveComparator
            implements Comparator<IASString>, java.io.Serializable {
        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(IASString s1, IASString s2) {
            return s1.compareToIgnoreCase(s2);
        }
    }

}
