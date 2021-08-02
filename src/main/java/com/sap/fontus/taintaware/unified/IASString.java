package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.utils.stats.Statistics;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SuppressWarnings("ALL")
public final class IASString implements IASTaintAware, Comparable<IASString>, CharSequence {
    private final static String SPLIT_LINE_REGEX = "(\\r|\\n|\\r\\n)";

    private String string;
    private IASTaintInformationable taintInformation;

    public IASString() {
        this.string = "";
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.addRangeCount(null);
        }
    }

    public IASString(byte bytes[], int offset, int length, IASString charsetName)
            throws UnsupportedEncodingException {
        this.string = new String(bytes, offset, length, charsetName.getString());
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.addRangeCount(null);
        }
    }


    public IASString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        this.string = s;
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.addRangeCount(null);
        }
    }

    public IASString(String s, boolean tainted) {
        this(s);
        setTaint(tainted);
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.addRangeCount(this.taintInformation);
        }
    }

    public IASString(IASAbstractStringBuilder strb) {
        IASString s = strb.toIASString();
        this.string = s.getString();
        this.taintInformation = s.getTaintInformationCopied();
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.addRangeCount(this.taintInformation);
            Statistics.INSTANCE.incrementInitialized();
        }
    }

    public IASString(String s, IASTaintInformationable taintInformation) {
        this(s);
        this.taintInformation = taintInformation == null ? null : taintInformation.copy();
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.addRangeCount(taintInformation);
        }
    }

    public IASString(CharSequence sequence, IASTaintInformationable taintInformation) {
        this(sequence.toString());
        this.taintInformation = taintInformation == null ? null : taintInformation.copy();
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.addRangeCount(this.taintInformation);
        }
    }

    @Override
    public boolean isTainted() {
        if (this.taintInformation == null) {
            return false;
        }
        return this.taintInformation.isTainted();
    }

    @Override
    public boolean isTaintedAt(int index) {
        if (isUninitialized()) {
            return false;
        }
        return this.taintInformation.getTaint(index) != null;
    }

    public static IASString tainted(IASString tstr) {
        if (tstr != null) {
            tstr.setTaint(true);
        }
        return tstr;
    }

    /**
     * Marks the whole string as tainted.
     * If the string already has tainted parts, they will be removed and replaced with one range tainting all.
     *
     * @param taint
     */
    @Override
    public void setTaint(boolean taint) {
        setTaint(taint ? IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN : null);
    }

    @Override
    public void setTaint(IASTaintSource source) {
        if (isTainted()) {
            this.taintInformation = TaintInformationFactory.createTaintInformation(this.length());
        }
        if (source != null) {
            if (isUninitialized()) {
                if (Configuration.getConfiguration().collectStats()) {
                    Statistics.INSTANCE.incrementInitialized();
                }
                this.taintInformation = TaintInformationFactory.createTaintInformation(this.length());
            }
            this.taintInformation = this.taintInformation.addRange(0, this.string.length(), source);
        } else {
            this.taintInformation = null;
        }
    }

    @Override
    public void setContent(String content, IASTaintInformationable taintInformation) {
        this.string = content;
        this.taintInformation = taintInformation == null ? null : taintInformation.copy();
    }

    public void initialize() {
        if (isUninitialized()) {
            this.taintInformation = TaintInformationFactory.createTaintInformation(this.length());

            if (Configuration.getConfiguration().collectStats() && isUninitialized()) {
                Statistics.INSTANCE.incrementInitialized();
            }
        }
    }

    public IASString(char value[]) {
        this(new String(value));
    }

    public IASString(char value[], int offset, int count) {
        this(new String(value, offset, count));
    }

    public IASString(int[] codePoints, int offset, int count) {
        this(new String(codePoints, offset, count));
    }

    public IASString(byte ascii[], int hibyte, int offset, int count) {
        this(new String(ascii, hibyte, offset, count));
    }

    public IASString(byte ascii[], int hibyte) {
        this(new String(ascii, hibyte));
    }

    public IASString(byte bytes[], int offset, int length, String charsetName)
            throws UnsupportedEncodingException {
        this(new String(bytes, offset, length, charsetName));
    }

    public IASString(byte bytes[], int offset, int length, Charset charset) {
        this(new String(bytes, offset, length, charset));
    }

    public IASString(byte bytes[], IASString charsetName) throws UnsupportedEncodingException {
        this(new String(bytes, charsetName.getString()));
    }

    public IASString(byte bytes[], Charset charset) {
        this(new String(bytes, charset));
    }

    public IASString(byte bytes[], int offset, int length) {
        this(new String(bytes, offset, length));
    }

    public IASString(byte[] bytes) {
        this(new String(bytes));
    }

    public IASString(StringBuffer buffer) {
        this(new String(buffer));
    }

    public IASString(IASStringBuilder builder) {
        this(builder.toString(), builder.getTaintInformationCopied());
    }

    public IASString(IASStringBuffer buffer) {
        this(buffer.toString(), buffer.getTaintInformationCopied());
    }

    public IASString(IASString string) {
        this(string.string, string.getTaintInformationCopied());
    }

    /**
     * Creates a new taintable String from a charsequence.
     * If it's marked as tainted, the whole string will be marked as tainted
     *
     * @param cs
     * @param tainted
     */
    private IASString(CharSequence cs, boolean tainted) {
        this(cs.toString(), tainted);
    }

    public int length() {
        return this.string.length();
    }

    public boolean isEmpty() {
        return this.string.isEmpty();
    }

    public char charAt(int index) {
        return this.string.charAt(index);
    }

    public int codePointAt(int index) {
        return this.string.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return this.string.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return this.string.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return this.string.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        this.string.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
        this.string.getBytes(srcBegin, srcEnd, dst, dstBegin);
    }

    public byte[] getBytes(IASString charsetName) throws UnsupportedEncodingException {
        return this.string.getBytes(charsetName.getString());
    }

    public byte[] getBytes(Charset charset) {
        return this.string.getBytes(charset);
    }

    public byte[] getBytes() {
        return this.string.getBytes();
    }

    public boolean equals(Object anObject) {
        if (anObject instanceof String) return this.string.equals(anObject);
        if (!(anObject instanceof IASString)) return false;
        IASString other = (IASString) anObject;
        return this.string.equals(other.string);
    }

    public boolean contentEquals(IASAbstractStringBuilder sb) {
        return this.string.contentEquals(sb);
    }

    public boolean contentEquals(StringBuffer sb) {
        return this.string.contentEquals(sb);
    }

    public boolean contentEquals(CharSequence cs) {
        return this.string.contentEquals(cs);
    }

    public boolean equalsIgnoreCase(IASString anotherString) {
        if (anotherString == null) {
            return false;
        }
        return this.string.equalsIgnoreCase(anotherString.getString());
    }

    @Override
    public int compareTo(IASString anotherString) {
        return this.string.compareTo(anotherString.getString());
    }

    public int compareToIgnoreCase(IASString str) {
        return this.string.compareToIgnoreCase(str.getString());
    }

    public boolean regionMatches(int toffset, IASString other, int ooffset, int len) {
        return this.string.regionMatches(toffset, ((IASString) other).string, ooffset, len);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, IASString other, int ooffset, int len) {
        return this.string.regionMatches(ignoreCase, toffset, other.getString(), ooffset, len);
    }

    public boolean startsWith(IASString prefix, int toffset) {
        return this.string.startsWith(prefix.getString(), toffset);
    }

    public boolean startsWith(IASString prefix) {
        return this.string.startsWith(prefix.getString());
    }

    public boolean endsWith(IASString suffix) {
        return this.string.endsWith(suffix.getString());
    }

    //TODO: sound?
    public int hashCode() {
        return this.string.hashCode();
    }

    public int indexOf(int ch) {
        return this.string.indexOf(ch);
    }

    public int indexOf(int ch, int fromIndex) {
        return this.string.indexOf(ch, fromIndex);
    }

    public int lastIndexOf(int ch) {
        return this.string.lastIndexOf(ch);
    }

    public int lastIndexOf(int ch, int fromIndex) {
        return this.string.lastIndexOf(ch, fromIndex);
    }

    public int indexOf(IASString str) {
        return this.string.indexOf(str.getString());
    }

    public int indexOf(IASString str, int fromIndex) {
        return this.string.indexOf(str.getString(), fromIndex);
    }

    public int lastIndexOf(IASString str) {
        return this.string.lastIndexOf(str.getString());
    }

    public int lastIndexOf(IASString str, int fromIndex) {
        return this.string.lastIndexOf(str.getString(), fromIndex);
    }

    public IASString substring(int beginIndex) {
        IASTaintInformationable newInfo = this.taintInformation != null ? taintInformation.slice(beginIndex, this.string.length()) : null;
        return new IASString(this.string.substring(beginIndex), newInfo);
    }

    public IASString substring(int beginIndex, int endIndex) {
        if (beginIndex < 0 || this.length() < endIndex || endIndex < beginIndex) {
            throw new IllegalArgumentException("startIndex: " + beginIndex + ", endIndex: " + endIndex);
        }

        if (beginIndex == endIndex) {
            return new IASString();
        }

        IASTaintInformationable newInfo = this.taintInformation != null ? taintInformation.slice(beginIndex, endIndex) : null;
        return new IASString(this.string.substring(beginIndex, endIndex), newInfo);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        IASTaintInformationable newInfo = this.taintInformation != null ? taintInformation.slice(beginIndex, endIndex) : null;
        return new IASString(this.string.subSequence(beginIndex, endIndex), newInfo);
    }

    public IASString concat(IASString str) {
        IASTaintInformationable taintInformation = null;
        if (this.taintInformation != null || str.taintInformation != null) {
            taintInformation = this.taintInformation == null ? TaintInformationFactory.createTaintInformation(this.string.length()) : this.taintInformation.copy();
            taintInformation = taintInformation.insertWithShift(this.string.length(), str.getTaintInformationInitialized());
        }
        return new IASString(this.string.concat(str.getString()), taintInformation);
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
        return new IASString(this.string.replace(oldChar, newChar), this.getTaintInformationCopied());
    }

    public boolean matches(IASString regex) {
        return this.string.matches(regex.getString());
    }

    public boolean contains(CharSequence s) {
        return this.string.contains(s);
    }

    public IASString replaceFirst(IASString regex, IASString replacement) {
        return IASPattern.compile(regex).matcher(this).replaceFirst(replacement);
    }

    public IASString replaceAll(IASString regex, IASString replacement) {
        return IASPattern.compile(regex).matcher(this).replaceAll(replacement);
    }

    public IASString replace(CharSequence target, CharSequence replacement) {
        IASString replString = IASString.valueOf(replacement);

        if (!this.isTainted() && !replString.isTainted()) {
            return new IASString(this.string.replace(target, replacement));
        }

        IASStringBuilder builder = new IASStringBuilder();

        int end = 0;
        int start = 0;
        for (start = this.string.indexOf(target.toString()); start >= 0; start = this.string.indexOf(target.toString(), start + 1)) {
            IASString beginStr = this.substring(end, start);
            builder.append(beginStr);
            end = start + target.length();
            builder.append(replString);
        }

        if (end < this.length()) {
            builder.append(this.substring(end));
        }

        return builder.toIASString();
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASString regex, int limit) {
        return IASPattern.compile(regex).split(this, limit);
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASString regex) {
        return this.split(regex, 0);
    }

    public static IASString join(CharSequence delimiter, CharSequence... elements) {
        if (elements == null || elements.length == 0) {
            return new IASString();
        } else if (elements.length == 1) {
            return IASString.valueOf(elements[0]);
        } else {
            IASString iasDelimiter = IASString.valueOf(delimiter);
            IASStringBuilder sb = new IASStringBuilder(elements[0]);

            for (int i = 1; i < elements.length; i++) {
                sb.append(iasDelimiter);
                sb.append(IASString.valueOf(elements[i]));
            }
            return sb.toIASString();
        }
    }


    public static IASString join(CharSequence delimiter,
                                 Iterable<? extends CharSequence> elements) {
        ArrayList<CharSequence> l = new ArrayList();
        for (CharSequence s : elements) {
            l.add(s);
        }
        return IASString.join(delimiter, l.toArray(new CharSequence[l.size()]));
    }

    public IASString toLowerCase(Locale locale) {
        return new IASString(this.string.toLowerCase(locale), this.getTaintInformationCopied());
    }

    public IASString toLowerCase() {
        return new IASString(this.string.toLowerCase(), this.getTaintInformationCopied());
    }

    public IASString toUpperCase(Locale locale) {
        return new IASString(this.string.toUpperCase(locale), this.getTaintInformationCopied());
    }

    public IASString toUpperCase() {
        return new IASString(this.string.toUpperCase(), this.getTaintInformationCopied());
    }

    public IASString trim() {
        String newStr = this.string.trim();
        int start = this.string.indexOf(newStr);
        int end = start + newStr.length();
        return this.substring(start, end);
    }

    /* JDK 11 BEGIN */
    public IASString strip() {
        String newStr = this.string.strip();
        int start = this.string.indexOf(newStr);
        int end = start + newStr.length();
        return this.substring(start, end);
    }

    public IASString stripLeading() {
        String newStr = this.string.stripLeading();
        int start = this.string.indexOf(newStr);
        int end = start + newStr.length();
        return this.substring(start, end);
    }

    public IASString stripTrailing() {
        String newStr = this.string.stripTrailing();
        int start = this.string.indexOf(newStr);
        int end = start + newStr.length();
        return this.substring(start, end);
    }

    public boolean isBlank() {
        return this.string.isBlank();
    }

    public Stream<IASString> lines() {
        return Arrays.stream(this.split(new IASString(IASString.SPLIT_LINE_REGEX)));
    }

    public IASString repeat(int count) {
        IASStringBuilder stringBuilder = new IASStringBuilder();
        for (int i = 0; i < count; i++) {
            stringBuilder.append(this);
        }
        return stringBuilder.toIASString();
    }
    /* JDK 11 END */

    //TODO: sound?
    public String toString() {
        return this.string.toString();
    }

    public IASString toIASString() {
        return this;
    }

    @Override
    public boolean isInitialized() {
        return this.taintInformation != null;
    }

    public IntStream chars() {
        return this.string.chars();
    }

    public IntStream codePoints() {
        return this.string.codePoints();
    }

    public char[] toCharArray() {
        return this.string.toCharArray();
    }

    private static boolean isTainted(Object[] args) {
        boolean isTainted = false;
        if (args != null) {
            for (Object o : args) {
                if (o instanceof IASTaintAware) {
                    IASTaintAware ta = (IASTaintAware) o;
                    isTainted |= ta.isTainted();
                }
            }
        }
        return isTainted;
    }

    public static IASString format(IASString format, Object... args) {
        // TODO Implement rainting
//        return new IASString(String.format(format.toString(), args), isTainted(args));
        return new IASFormatter().format(format, args).toIASString();
    }


    public static IASString format(Locale l, IASString format, Object... args) {
        // TODO Implement rainting
//        return new IASString(String.format(l, format.toString(), args), isTainted(args));
        return new IASFormatter(l).format(format, args).toIASString();
    }

    public static IASString valueOf(Object obj) {
        if (obj instanceof IASString) {
            return (IASString) obj;
        } else if (obj instanceof IASStringBuffer) {
            return ((IASStringBuffer) obj).toIASString();
        } else if (obj instanceof IASStringBuilder) {
            return ((IASStringBuilder) obj).toIASString();
        } else {
            return new IASString(String.valueOf(obj));
        }
    }

    public static IASString valueOf(CharSequence s, int start, int end) {
        if (s instanceof IASString) {
            return ((IASString) s).substring(start, end);
        } else {
            return IASString.valueOf(s.subSequence(start, end));
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

    public IASString intern() {
        this.string = this.string.intern();
        return (IASString) IASStringPool.intern(this);
    }

    public static IASString fromString(String str) {
        if (str == null) {
            return null;
        }
        return new IASString(str);
    }

    public static String asString(IASString str) {
        if (str == null) {
            return null;
        }
        return str.string;
    }

    public static IASString toStringOf(Object o) {
        if (o instanceof IASTaintAware) {
            return (IASString) ((IASTaintAware) o).toIASString();
        } else {
            String val = o.toString();
            if (val == null) {
                return null;
            } else {
                return new IASString(val);
            }
        }
    }

    public String getString() {
        return this.string;
    }

    public IASTaintInformationable getTaintInformation() {
        return taintInformation;
    }

    public IASTaintInformationable getTaintInformationInitialized() {
        return taintInformation != null ? this.taintInformation : TaintInformationFactory.createTaintInformation(this.string.length());
    }

    public IASTaintInformationable getTaintInformationCopied() {
        return taintInformation != null ? this.taintInformation.copy() : null;
    }

    public boolean isUninitialized() {
        return this.taintInformation == null;
    }

    public static final Comparator<IASString> CASE_INSENSITIVE_ORDER
            = new CaseInsensitiveComparator();

    private static class CaseInsensitiveComparator
            implements Comparator<IASString>, java.io.Serializable {

        public int compare(IASString s1, IASString s2) {
            return s1.compareToIgnoreCase(s2);
        }
    }
}
