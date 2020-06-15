package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations.*;
import de.tubs.cs.ias.asm_test.taintaware.shared.*;
import de.tubs.cs.ias.asm_test.taintaware.shared.range.IASTaintRangeStringable;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("Since15")
public class IASString implements IASTaintRangeStringable, IASLazyComplexAware {
    private final String string;
    private IASTaintInformation taintInformation;

    public IASString() {
        this.string = "";
        this.taintInformation = null;
    }

    public IASString(String string) {
        this.string = string;
        this.taintInformation = null;
    }

    public IASString(String string, IASTaintInformation taintInformation) {
        this.string = string;
        this.taintInformation = taintInformation;
    }

    public IASString(String s, boolean tainted) {
        this(s);
        setTaint(tainted);
    }

    public IASString(String s, List<IASTaintRange> ranges) {
        this(s, new IASTaintInformation(new BaseOperation(ranges)));
    }

    public IASString(CharSequence sequence) {
        this(sequence.toString());
    }

    public IASString(CharSequence sequence, List<IASTaintRange> ranges) {
        this(sequence.toString(), new IASTaintInformation(new BaseOperation(ranges)));
    }

    public static IASString tainted(IASString tstr) {
        tstr.setTaint(true);
        return tstr;
    }

    public IASString(char[] value) {
        this(new String(value));
    }

    public IASString(char[] value, int offset, int count) {
        this(new String(value, offset, count));
    }

    public IASString(int[] codePoints, int offset, int count) {
        this(new String(codePoints, offset, count));
    }

    public IASString(byte[] ascii, int hibyte, int offset, int count) {
        this(new String(ascii, hibyte, offset, count));
    }

    public IASString(byte[] ascii, int hibyte) {
        this(new String(ascii, hibyte));
    }

    public IASString(byte[] bytes, int offset, int length, String charsetName)
            throws UnsupportedEncodingException {
        this(new String(bytes, offset, length, charsetName));
    }

    public IASString(byte[] bytes, int offset, int length, Charset charset) {
        this(new String(bytes, offset, length, charset));
    }

    public IASString(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
        this(new String(bytes, charsetName));
    }

    public IASString(byte[] bytes, Charset charset) {
        this(new String(bytes, charset));
    }

    public IASString(byte[] bytes, int offset, int length) {
        this(new String(bytes, offset, length));
    }

    public IASString(byte[] bytes) {
        this(new String(bytes));
    }

    public IASString(IASStringBuilderable builder) {
        this(builder.toString(), new IASTaintInformation(new BaseOperation(((IASAbstractStringBuilder) builder).getTaintRanges())));
    }

    public IASString(IASStringBuilder builder) {
        this(builder.toString(), new IASTaintInformation(new BaseOperation(builder.getTaintRanges())));
    }

    public IASString(IASStringBuffer builder) {
        this(builder.toString(), new IASTaintInformation(new BaseOperation(((IASAbstractStringBuilder) builder).getTaintRanges())));
    }

    public IASString(IASStringable string) {
        this(string.getString(), ((IASString) string).taintInformation);
    }

    public IASString(IASString string) {
        this(string.getString(), string.taintInformation);
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

    public boolean isInitialized() {
        return this.taintInformation != null;
    }

    public boolean isUninitialized() {
        return !this.isInitialized();
    }

    @Override
    public void initialize() {
        if (this.isUninitialized()) {
            this.taintInformation = new IASTaintInformation();
        }
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        if (isUninitialized()) {
            return new ArrayList<>();
        }
        return this.taintInformation.getTaintRanges();
    }

    @Override
    public void abortIfTainted() {

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
        return this.string.codePointAt(index);
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
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        this.string.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
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
        return this.string.contentEquals(sb.getBuilder());
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
        return this.string.contentEquals(cs);
    }

    @Override
    public boolean equalsIgnoreCase(IASStringable anotherString) {
        return this.string.equalsIgnoreCase(anotherString.getString());
    }

    @Override
    public int compareToIgnoreCase(IASStringable str) {
        return this.string.compareToIgnoreCase(str.getString());
    }

    @Override
    public boolean regionMatches(int toffset, IASStringable other, int ooffset, int len) {
        return this.string.regionMatches(ooffset, other.getString(), ooffset, len);
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
        return this.string.indexOf(str.getString());
    }

    @Override
    public int lastIndexOf(IASStringable str, int fromIndex) {
        return this.string.lastIndexOf(str.getString(), fromIndex);
    }

    @Override
    public IASString substring(int beginIndex) {
        String substringed = this.string.substring(beginIndex);
        return this.derive(substringed, new SubstringOperation(beginIndex), false);
    }

    @Override
    public IASString substring(int beginIndex, int endIndex) {
        String substringed = this.string.substring(beginIndex, endIndex);
        return this.derive(substringed, new SubstringOperation(beginIndex, endIndex), false);
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        String substringed = this.string.substring(beginIndex, endIndex);
        return this.derive(substringed, new SubstringOperation(beginIndex, endIndex), false);
    }

    @Override
    public IASString concat(IASStringable str) {
        String substringed = this.string.concat(str.getString());
        return this.derive(substringed, new ConcatOperation((IASLazyComplexAware) str), ((IASString) str).isInitialized());
    }

    @Override
    public IASString replace(char oldChar, char newChar) {
        return this.derive(this.string.replace(oldChar, newChar), new ReplaceCharacterOperation(oldChar), false);
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
        String replaced = this.string.replaceFirst(regex.getString(), replacement.getString());
        return this.derive(replaced, new ReplaceFirstOperation((IASString) regex, (IASString) replacement), ((IASString) replacement).isInitialized());
    }

    @Override
    public IASString replaceAll(IASStringable regex, IASStringable replacement) {
        String replaced = this.string.replaceAll(regex.getString(), replacement.getString());
        return this.derive(replaced, new ReplaceAllOperation((IASString) regex, (IASString) replacement), ((IASString) replacement).isInitialized());
    }

    @Override
    public IASString replace(CharSequence target, CharSequence replacement) {
        return this.derive(this.string.replace(target, replacement), new ReplaceCharSequenceOperation(IASString.valueOf(target), IASString.valueOf(replacement)), ((IASString) replacement).isInitialized());
    }

    @Override
    public IASString[] split(IASStringable regex, int limit) {
        return IASPattern.compile(regex).split(this, limit);
    }

    @Override
    public IASString[] split(IASStringable regex) {
        return this.split(regex, 0);
    }

    @Override
    public IASString toLowerCase(Locale locale) {
        return new IASString(this.string.toLowerCase(locale), this.taintInformation);
    }

    @Override
    public IASString toLowerCase() {
        return new IASString(this.string.toLowerCase(), this.taintInformation);
    }

    @Override
    public IASString toUpperCase(Locale locale) {
        return new IASString(this.string.toUpperCase(locale), this.taintInformation);
    }

    @Override
    public IASString toUpperCase() {
        return new IASString(this.string.toUpperCase(), this.taintInformation);
    }

    @Override
    public IASString trim() {
        String trimmed = this.string.trim();
        return this.derive(trimmed, new TrimOperation(), false);
    }

    @Override
    public IASString strip() {
        return this.derive(this.string.strip(), new StripOperation(true, true), false);
    }

    @Override
    public IASString stripLeading() {
        return this.derive(this.string.stripLeading(), new StripOperation(true, false), false);
    }

    @Override
    public IASString stripTrailing() {
        return this.derive(this.string.stripTrailing(), new StripOperation(false, true), false);
    }

    @Override
    public boolean isBlank() {
        return this.string.isBlank();
    }

    @Override
    public IASString repeat(int count) {
        return this.derive(this.string.repeat(count), new RepeatOperation(count), false);
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

    @Override
    public IASString intern() {
        return (IASString) IASStringPool.intern(this);
    }

    @Override
    public String getString() {
        return this.string;
    }

    @Override
    public Stream<IASString> lines() {
        return Arrays.stream(this.split(new IASString("(\\n|\\r)")));
    }

    @Override
    public boolean isTainted() {
        if (isUninitialized()) {
            return false;
        }
        return this.taintInformation.isTainted();
    }

    @Override
    public void setTaint(boolean taint) {
        if (taint != this.isTainted()) {
            if (taint) {
                this.taintInformation = new IASTaintInformation(new BaseOperation(0, this.length(), IASTaintSource.TS_CS_UNKNOWN_ORIGIN));
            } else {
                this.taintInformation = null;
            }
        }
    }

    @Override
    public int compareTo(IASStringable o) {
        return this.string.compareTo(o.getString());
    }

    public static IASString join(CharSequence delimiter, CharSequence... elements) {
        if (elements == null || elements.length == 0) {
            return new IASString();
        } else if (elements.length == 1) {
            return IASString.valueOf(elements[0]);
        } else {
            IASString begin = IASString.valueOf(elements[0]);
            IASString iasDelimiter = IASString.valueOf(delimiter);
            IASStringBuilder sb = new IASStringBuilder(begin);

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
        return new IASFormatter().format(format, args).toIASString();
    }


    public static IASString format(Locale l, IASStringable format, Object... args) {
        // TODO Implement rainting
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

    public static IASString valueOf(char[] data) {
        return new IASString(String.valueOf(data));
    }

    public static IASString valueOf(char[] data, int offset, int count) {
        return new IASString(String.valueOf(data, offset, count));
    }

    public static IASString copyValueOf(char[] data, int offset, int count) {
        return new IASString(String.copyValueOf(data, offset, count));
    }

    public static IASString copyValueOf(char[] data) {
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

    @Override
    public String toString() {
        return this.string;
    }

    IASString derive(String newString, IASOperation operation, boolean initializeIfNecessary) {
        if (this.isInitialized()) {
            return new IASString(newString, new IASTaintInformation(this.getString(), this.taintInformation, operation));
        } else if (initializeIfNecessary) {
            return new IASString(newString, new IASTaintInformation(this.getString(), new IASTaintInformation(), operation));
        }
        return new IASString(newString);
    }
}
