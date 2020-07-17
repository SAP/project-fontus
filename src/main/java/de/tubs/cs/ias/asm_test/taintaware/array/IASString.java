package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.shared.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SuppressWarnings("ALL")
public final class IASString implements IASArrayAware, IASStringable {
    private String string;
    private IASTaintInformation taintInformation;

    public IASString() {
        this.string = "";
    }

    public IASString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        this.string = s;
    }

    public IASString(String s, boolean tainted) {
        this(s);
        setTaint(tainted);
    }

    public IASString(IASStringable s) {
        this((IASString) s);
    }

    public IASString(IASAbstractStringBuilderable s) {
        this(s.toIASString());
    }

    public IASString(String s, int[] taints) {
        this(s);
        this.setTaints(taints);
    }

    private void setTaints(int[] taints) {
        if (taints.length != this.length()) {
            throw new IllegalArgumentException("Taint array size must match string length!");
        }
        this.taintInformation = new IASTaintInformation(taints);
    }

    public IASString(CharSequence sequence, int[] taints) {
        this(sequence.toString());
        this.taintInformation = new IASTaintInformation(taints);
    }

    @Override
    public boolean isTainted() {
        if (this.taintInformation == null) {
            return false;
        }
        return this.taintInformation.isTainted();
    }

    public static IASString tainted(IASString tstr) {
        tstr.setTaint(true);
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
        setTaint(taint ? IASTaintSource.TS_CS_UNKNOWN_ORIGIN : null);
    }

    @Override
    public void setTaint(IASTaintSource source) {
        if (source != null) {
            if (isUninitialized()) {
                this.initialize();
            }
            this.taintInformation.setTaint(0, this.string.length(), source);
        } else {
            this.taintInformation = null;
        }
    }

    @Override
    public void setTaint(List<IASTaintRange> ranges) {
        if (ranges == null || ranges.size() == 0) {
            this.taintInformation = null;
        } else {
            int[] taint = TaintConverter.toTaintArray(this.length(), ranges);
            this.taintInformation = new IASTaintInformation(taint);
        }
    }

    public void initialize() {
        if (isUninitialized()) {
            this.taintInformation = new IASTaintInformation(this.length());
        }
    }

    @Override
    public boolean isTaintedAt(int index) {
        if (isUninitialized()) {
            return false;
        }
        return this.taintInformation.isTaintedAt(index);
    }

    public IASString(String s, IASTaintInformation iasTaintInformation) {
        this(s);
        this.taintInformation = iasTaintInformation.clone();
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

    public IASString(byte bytes[], String charsetName) throws UnsupportedEncodingException {
        this(new String(bytes, charsetName));
    }

    public IASString(byte bytes[], IASStringable charsetName) throws UnsupportedEncodingException {
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
        this(builder.toString());
        if (builder.isInitialized()) {
            this.taintInformation = builder.getTaintInformation().clone();
        }
    }

    public IASString(IASStringBuffer buffer) {
        this(buffer.toString());
        if (buffer.isInitialized()) {
            this.taintInformation = buffer.getTaintInformation().clone();
        }
    }

    public IASString(IASString string) {
        this(string.string);
        if (string.isInitialized()) {
            this.taintInformation = string.taintInformation.clone();
        }
    }

    /**
     * Creates a new taintable String from a charsequence.
     * If it's marked as tainted, the whole string will be marked as tainted
     *
     * @param cs
     * @param tainted
     */
    private IASString(CharSequence cs, boolean tainted) {
        this(cs.toString());
        this.setTaint(tainted);
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

    public byte[] getBytes(IASStringable charsetName) throws UnsupportedEncodingException {
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

    public boolean contentEquals(IASAbstractStringBuilderable sb) {
        return this.string.contentEquals(sb);
    }

    public boolean contentEquals(StringBuffer sb) {
        return this.string.contentEquals(sb);
    }

    public boolean contentEquals(CharSequence cs) {
        return this.string.contentEquals(cs);
    }

    public boolean equalsIgnoreCase(IASStringable anotherString) {
        if (anotherString == null) {
            return false;
        }
        return this.string.equalsIgnoreCase(anotherString.getString());
    }

    @Override
    public int compareTo(IASStringable anotherString) {
        return this.string.compareTo(anotherString.getString());
    }

    public int compareToIgnoreCase(IASStringable str) {
        return this.string.compareToIgnoreCase(str.getString());
    }

    public boolean regionMatches(int toffset, IASStringable other, int ooffset, int len) {
        return this.string.regionMatches(toffset, other.getString(), ooffset, len);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, IASStringable other, int ooffset, int len) {
        return this.string.regionMatches(ignoreCase, toffset, other.getString(), ooffset, len);
    }

    public boolean startsWith(IASStringable prefix, int toffset) {
        return this.string.startsWith(prefix.getString(), toffset);
    }

    public boolean startsWith(IASStringable prefix) {
        return this.string.startsWith(prefix.getString());
    }

    public boolean endsWith(IASStringable suffix) {
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

    public int indexOf(IASStringable str) {
        return this.string.indexOf(str.getString());
    }

    public int indexOf(IASStringable str, int fromIndex) {
        return this.string.indexOf(str.getString(), fromIndex);
    }

    public int lastIndexOf(IASStringable str) {
        return this.string.lastIndexOf(str.getString());
    }

    public int lastIndexOf(IASStringable str, int fromIndex) {
        return this.string.lastIndexOf(str.getString(), fromIndex);
    }

    private int[] getSubstringTaint(int beginIndex, int endIndex) {
        if (this.isUninitialized()) {
            return new int[endIndex - beginIndex];
        }
        return this.taintInformation.getTaints(beginIndex, endIndex);
    }

    public IASString substring(int beginIndex) {
        int[] taints = this.getSubstringTaint(beginIndex, this.length());
        return new IASString(this.string.substring(beginIndex), taints);
    }

    public IASString substring(int beginIndex, int endIndex) {
        if (beginIndex < 0 || this.length() < endIndex || endIndex < beginIndex) {
            throw new IllegalArgumentException("startIndex: " + beginIndex + ", endIndex: " + endIndex);
        }

        if (beginIndex == endIndex) {
            return new IASString();
        }
        int[] taints = this.getSubstringTaint(beginIndex, endIndex);
        return new IASString(this.string.substring(beginIndex, endIndex), taints);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        int[] taints = this.getSubstringTaint(beginIndex, endIndex);
        return new IASString(this.string.subSequence(beginIndex, endIndex), taints);
    }

    public IASString concat(IASStringable str) {
        int[] firstTaint = this.getTaints();
        int[] secondTaint = ((IASString) str).getTaints();
        int[] newTaint = new int[firstTaint.length + secondTaint.length];
        System.arraycopy(firstTaint, 0, newTaint, 0, firstTaint.length);
        System.arraycopy(secondTaint, 0, newTaint, firstTaint.length, secondTaint.length);
        IASString newStr = new IASString(this.string.concat(str.getString()), newTaint);
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
        return new IASString(this.string.replace(oldChar, newChar), getTaints());
    }

    public boolean matches(IASStringable regex) {
        return this.string.matches(regex.getString());
    }

    public boolean contains(CharSequence s) {
        return this.string.contains(s);
    }

    public IASString replaceFirst(IASStringable regex, IASStringable replacement) {
        return IASPattern.compile((IASString) regex).matcher(this).replaceFirst((IASString) replacement);
    }

    public IASString replaceAll(IASStringable regex, IASStringable replacement) {
        return IASPattern.compile((IASString) regex).matcher(this).replaceAll((IASString) replacement);
    }

    public IASString replace(CharSequence target, CharSequence replacement) {
        int start = this.string.indexOf(target.toString());
        if (start < 0) {
            return this;
        }
        IASString beginStr = (IASString) this.substring(0, start);

        int end = start + target.length();
        IASString endStr = (IASString) this.substring(end).replace(target, replacement);

        return beginStr.concat(IASString.valueOf(replacement)).concat(endStr);
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASStringable regex, int limit) {
        return IASPattern.compile((IASString) regex).split(this, limit);
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASStringable regex) {
        return this.split(regex, 0);
    }

    public static IASString join(CharSequence delimiter, CharSequence... elements) {
        if (elements == null || elements.length == 0) {
            return new IASString();
        } else if (elements.length == 1) {
            return IASString.valueOf(elements[0]);
        } else {
            IASString iasDelimiter = (IASString) IASString.valueOf(delimiter);
            IASStringBuilder sb = new IASStringBuilder(elements[0]);

            for (int i = 1; i < elements.length; i++) {
                sb.append(iasDelimiter);
                sb.append(IASString.valueOf(elements[i]));
            }
            return (IASString) sb.toIASString();
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
        return new IASString(this.string.toLowerCase(locale), this.getTaints());
    }

    public IASString toLowerCase() {
        return new IASString(this.string.toLowerCase(), this.getTaints());
    }

    public IASString toUpperCase(Locale locale) {
        return new IASString(this.string.toUpperCase(locale), this.getTaints());
    }

    public IASString toUpperCase() {
        return new IASString(this.string.toUpperCase(), this.getTaints());
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

    public Stream<IASStringable> lines() {
        return Arrays.stream(this.split(new IASString(IASStringable.SPLIT_LINE_REGEX)));
    }

    @Override
    public IASTaintSource getTaintFor(int position) {
        if (isUninitialized()) {
            return null;
        }
        return this.taintInformation.getTaintFor(position);
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

    public static IASString format(IASStringable format, Object... args) {
        // TODO Implement rainting
//        return new IASString(String.format(format.toString(), args), isTainted(args));
        return new IASFormatter().format((IASString) format, args).toIASString();
    }


    public static IASString format(Locale l, IASStringable format, Object... args) {
        // TODO Implement rainting
//        return new IASString(String.format(l, format.toString(), args), isTainted(args));
        return new IASFormatter(l).format((IASString) format, args).toIASString();
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

    public static IASString valueOf(CharSequence s, int start, int end) {
        if (s instanceof IASString) {
            return (IASString) ((IASString) s).substring(start, end);
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

    public static IASStringable copyValueOf(char data[], int offset, int count) {
        return new IASString(String.copyValueOf(data, offset, count));
    }

    public static IASStringable copyValueOf(char data[]) {
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

    public String getString() {
        return this.string;
    }

    public IASTaintInformation getTaintInformation() {
        return taintInformation;
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        if (isUninitialized()) {
            return Collections.emptyList();
        }
        return TaintConverter.toTaintRanges(this.taintInformation.getTaints());
    }

    public boolean isUninitialized() {
        return this.taintInformation == null;
    }

    public boolean isInitialized() {
        return !this.isUninitialized();
    }

    public void abortIfTainted() {
        if (this.isTainted()) {
            System.err.printf("String %s is tainted!\nAborting..!\n", this.string);
            System.exit(1);
        }
    }

    public static final Comparator<IASString> CASE_INSENSITIVE_ORDER
            = new CaseInsensitiveComparator();

    @Override
    public int[] getTaints() {
        if (this.isUninitialized()) {
            return new int[this.length()];
        }
        return this.taintInformation.getTaints();
    }

    private static class CaseInsensitiveComparator
            implements Comparator<IASString>, java.io.Serializable {

        public int compare(IASString s1, IASString s2) {
            return s1.compareToIgnoreCase(s2);
        }
    }
}
