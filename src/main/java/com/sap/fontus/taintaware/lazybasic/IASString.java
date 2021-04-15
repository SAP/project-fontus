package com.sap.fontus.taintaware.lazybasic;

import com.sap.fontus.taintaware.lazybasic.operation.BaseLayer;
import com.sap.fontus.taintaware.lazybasic.operation.DeleteLayer;
import com.sap.fontus.taintaware.lazybasic.operation.InsertLayer;
import com.sap.fontus.taintaware.shared.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("Since15")
public final class IASString implements IASStringable, IASTaintRangeAware {
    private String string;
    private IASTaintInformation taintInformation;

    public IASString(String string, IASTaintInformation taintInformation) {
        this.string = Objects.requireNonNull(string);
        this.taintInformation = taintInformation;
    }

    public IASString() {
        this.string = "";
        this.taintInformation = null;
    }

    public IASString(String string) {
        this.string = Objects.requireNonNull(string);
        this.taintInformation = null;
    }

    public IASString(String s, boolean tainted) {
        this(s);
        setTaint(tainted);
    }

    public IASString(String s, List<IASTaintRange> ranges) {
        this(s, new IASTaintInformation(new BaseLayer(ranges)));
    }

    public IASString(CharSequence sequence) {
        this(sequence.toString());
    }

    public IASString(CharSequence sequence, List<IASTaintRange> ranges) {
        this(sequence.toString(), new IASTaintInformation(new BaseLayer(ranges)));
    }

    public static IASString tainted(IASString tstr) {
        if (tstr != null) {
            tstr.setTaint(true);
        }
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

    public IASString(byte[] bytes, IASStringable charsetName) throws UnsupportedEncodingException {
        this(new String(bytes, charsetName.getString()));
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

    public IASString(IASAbstractStringBuilderable builder) {
        this(builder.toString(), ((IASAbstractStringBuilder) builder).getTaintInformation());
    }

    public IASString(IASStringBuilder builder) {
        this(builder.toString(), builder.getTaintInformation());
    }

    public IASString(IASStringBuffer builder) {
        this(builder.toString(), builder.getTaintInformation());
    }

    public IASString(IASStringable string) {
        this(string.getString(), ((IASString) string).taintInformation);
    }

    public IASString(IASString string) {
        this(string.getString(), string.taintInformation);
    }

    public IASString(StringBuffer strb) {
        this(strb.toString());
    }

    public IASString(byte bytes[], int offset, int length, IASStringable charsetName)
            throws UnsupportedEncodingException {
        this.string = new String(bytes, offset, length, charsetName.getString());
    }

    public static IASString fromString(String s) {
        if (s == null) {
            return null;
        }
        return new IASString(s);
    }

    public static String asString(IASString string) {
        if (string == null) {
            return null;
        }
        return string.string;
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
    public boolean contentEquals(IASAbstractStringBuilderable sb) {
        return this.string.contentEquals(sb.getStringBuilder());
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
        return this.string.contentEquals(cs);
    }

    @Override
    public boolean equalsIgnoreCase(IASStringable anotherString) {
        if (anotherString == null) {
            return false;
        }
        return this.string.equalsIgnoreCase(anotherString.getString());
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

    private IASString derive(String newString, IASLayer layer) {
        return this.derive(newString, Collections.singletonList(layer));
    }

    IASString derive(String newString, List<IASLayer> layers) {
        if (layers == null || layers.isEmpty()) {
            return new IASString(newString, this.taintInformation);
        }
        return new IASString(newString, new IASTaintInformation(layers, this.taintInformation));
    }

    @Override
    public IASString substring(int beginIndex) {
        return this.derive(this.string.substring(beginIndex), new DeleteLayer(0, beginIndex));
    }

    @Override
    public IASString substring(int beginIndex, int endIndex) {
        return this.derive(this.string.substring(beginIndex, endIndex), Arrays.asList(new DeleteLayer(endIndex), new DeleteLayer(0, beginIndex)));
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return this.derive(this.string.substring(beginIndex, endIndex), Arrays.asList(new DeleteLayer(endIndex), new DeleteLayer(0, beginIndex)));
    }

    @Override
    public IASString concat(IASStringable str) {
        return this.derive(this.string.concat(str.getString()), new InsertLayer(this.string.length(), this.string.length() + str.length(), ((IASString) str).taintInformation));
    }

    @Override
    public IASString replace(char oldChar, char newChar) {
        String newString = this.string.replace(oldChar, newChar);
        int i = 0;
        List<IASLayer> layers = new ArrayList<>();
        while ((i = this.string.indexOf(oldChar, i + 1)) >= 0) {
            layers.add(new InsertLayer(i, i + 1));
            layers.add(new DeleteLayer(i, i + 1));
        }
        Collections.reverse(layers);
        return this.derive(newString, layers);
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
        return IASPattern.compile(regex).matcher(this).replaceFirst(replacement);
    }

    @Override
    public IASString replaceAll(IASStringable regex, IASStringable replacement) {
        return IASPattern.compile(regex).matcher(this).replaceAll(replacement);
    }

    @Override
    public IASString replace(CharSequence target, CharSequence replacement) {
        String newString = this.string.replace(target, replacement);
        int i = -1;
        List<IASLayer> layers = new ArrayList<>();
        final int difference = replacement.length() - target.length();
        int diffSum = 0;
        while ((i = this.string.indexOf(target.toString(), i + 1)) >= 0) {
            layers.add(new InsertLayer(diffSum + i, diffSum + i + replacement.length(), IASString.valueOf(replacement).taintInformation));
            layers.add(new DeleteLayer(diffSum + i, diffSum + i + target.length()));
            diffSum += difference;
        }
        Collections.reverse(layers);
        return this.derive(newString, layers);
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

    @Override
    public IASString[] split(IASStringable regex, int limit) {
        return IASPattern.compile(regex).split(this, limit);
    }

    @Override
    public IASString[] split(IASStringable regex) {
        return IASPattern.compile(regex).split(this);
    }

    @Override
    public IASString toLowerCase(Locale locale) {
        return this.derive(this.string.toLowerCase(locale), (List<IASLayer>) null);
    }

    @Override
    public IASString toLowerCase() {
        return this.derive(this.string.toLowerCase(), (List<IASLayer>) null);
    }

    @Override
    public IASString toUpperCase(Locale locale) {
        return this.derive(this.string.toUpperCase(locale), (List<IASLayer>) null);
    }

    @Override
    public IASString toUpperCase() {
        return this.derive(this.string.toUpperCase(), (List<IASLayer>) null);
    }

    @Override
    public IASString trim() {
        String newStr = this.string.trim();
        int start = this.string.indexOf(newStr);
        int end = start + newStr.length();
        return this.derive(newStr, Arrays.asList(new DeleteLayer(end), new DeleteLayer(0, start)));
    }

    @Override
    public IASString strip() {
        String newStr = this.string.strip();
        int start = this.string.indexOf(newStr);
        int end = start + newStr.length();
        return this.derive(newStr, Arrays.asList(new DeleteLayer(end), new DeleteLayer(0, start)));
    }

    @Override
    public IASString stripLeading() {
        String newStr = this.string.stripLeading();
        int start = this.string.indexOf(newStr);
        int end = start + newStr.length();
        return this.derive(newStr, Arrays.asList(new DeleteLayer(end), new DeleteLayer(0, start)));
    }

    @Override
    public IASString stripTrailing() {
        String newStr = this.string.stripTrailing();
        int start = this.string.indexOf(newStr);
        int end = start + newStr.length();
        return this.derive(newStr, Arrays.asList(new DeleteLayer(end), new DeleteLayer(0, start)));
    }

    @Override
    public boolean isBlank() {
        return this.string.isBlank();
    }

    @Override
    public IASString repeat(int count) {
        if (count == 0) {
            return new IASString();
        }
        String newStr = this.string.repeat(count);
        List<IASLayer> layers = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            layers.add(new InsertLayer(i * this.string.length(), (i + 1) * this.string.length(), this.taintInformation));
        }
        return this.derive(newStr, layers);
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
        this.string = this.string.intern();
        return (IASString) IASStringPool.intern(this);
    }

    @Override
    public String getString() {
        return this.string;
    }

    @Override
    public Stream<? extends IASStringable> lines() {
        return Arrays.stream(this.split(new IASString(IASStringable.SPLIT_LINE_REGEX)));
    }

    @Override
    public IASTaintSource getTaintFor(int position) {
        return new IASTaintRanges(this.getTaintRanges()).getTaintFor(position);
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        if (this.taintInformation == null) {
            return new ArrayList<>(0);
        }
        return this.taintInformation.getTaintRanges();
    }

    @Override
    public boolean isUninitialized() {
        return this.taintInformation == null;
    }

    @Override
    public void initialize() {
        this.taintInformation = new IASTaintInformation();
    }

    @Override
    public boolean isTaintedAt(int index) {
        if (isUninitialized()) {
            return false;
        }
        return this.taintInformation.isTaintedAt(index);
    }

    @Override
    public void setTaint(IASTaintSource source) {
        this.taintInformation = null;
        if (source != null && this.string.length() > 0) {
            this.taintInformation = new IASTaintInformation(new BaseLayer(0, this.string.length(), source));
        }
    }

    @Override
    public void setContent(String content, List<IASTaintRange> taintRanges) {
        this.string = content;
        this.setTaint(taintRanges);
    }

    @Override
    public void setTaint(List<IASTaintRange> ranges) {
        if (ranges == null || ranges.size() == 0) {
            this.taintInformation = null;
        } else {
            this.taintInformation = new IASTaintInformation(new BaseLayer(ranges));
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
    public void setTaint(boolean taint) {
        this.setTaint(taint ? IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN : null);
    }

    @Override
    public int compareTo(IASStringable iasStringable) {
        return this.string.compareTo(iasStringable.getString());
    }

    public IASTaintInformation getTaintInformation() {
        return this.taintInformation;
    }

    public static IASString valueOf(CharSequence s, int start, int end) {
        if (s instanceof IASString) {
            return (IASString) ((IASString) s).substring(start, end);
        } else {
            return IASString.valueOf(s.subSequence(start, end));
        }
    }

    public static IASString valueOf(char[] data) {
        return new IASString(String.valueOf(data));
    }

    public static IASString format(IASStringable toFormat, Object[] parameters) {
        return (IASString) new IASFormatter().format(toFormat, parameters).toIASString();
    }

    public static IASString format(Locale l, IASStringable toFormat, Object[] parameters) {
        return (IASString) new IASFormatter(l).format(toFormat, parameters).toIASString();
    }

    public static IASString join(CharSequence delimiter, CharSequence... elements) {
        if (elements == null || elements.length == 0) {
            return new IASString();
        } else if (elements.length == 1) {
            return IASString.valueOf(elements[0]);
        } else {
            IASString begin = IASString.valueOf(elements[0]);
            IASString iasDelimiter = IASString.valueOf(delimiter);

            List<IASLayer> layers = new ArrayList<>((elements.length - 1) * 2);
            int length = begin.length();
            for (int i = 1; i < elements.length; i++) {
                IASString string = IASString.valueOf(elements[i]);
                layers.add(new InsertLayer(length, length + iasDelimiter.length(), iasDelimiter.getTaintInformation()));
                length += iasDelimiter.length();
                layers.add(new InsertLayer(length, length + string.length(), string.getTaintInformation()));
                length += string.length();
            }
            return begin.derive(String.join(delimiter, elements), layers);
        }
    }


    public static IASString join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        ArrayList<CharSequence> l = new ArrayList<>();
        for (CharSequence s : elements) {
            l.add(s);
        }
        return IASString.join(delimiter, l.toArray(new CharSequence[0]));
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) return this.string.equals(obj);
        if (!(obj instanceof IASString)) return false;
        IASString other = (IASString) obj;
        return this.string.equals(other.string);
    }

    @Override
    public String toString() {
        return this.string;
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

    public boolean isInitialized() {
        return !isUninitialized();
    }

    public static final Comparator<IASString> CASE_INSENSITIVE_ORDER
            = new IASString.CaseInsensitiveComparator();

    private static class CaseInsensitiveComparator
            implements Comparator<IASString>, java.io.Serializable {
        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(IASString s1, IASString s2) {
            return s1.compareToIgnoreCase(s2);
        }
    }
}
