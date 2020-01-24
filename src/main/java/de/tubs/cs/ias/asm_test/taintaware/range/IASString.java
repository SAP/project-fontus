package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@SuppressWarnings("ALL")
public final class IASString implements IASRangeAware, Comparable<IASString>, CharSequence {

    private String str;
    private IASTaintInformation taintInformation;

    public IASString() {
        this.str = "";
    }

    public IASString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        this.str = s;
    }

    public IASString(String s, boolean tainted) {
        this(s);
        this.taintInformation = new IASTaintInformation();
        this.taintInformation.addRange(0, s.length(), (short) IASTaintSource.TS_CS_UNKNOWN_ORIGIN.getId());
    }

    public IASString(String s, List<IASTaintRange> ranges) {
        this(s);
        this.appendRangesFrom(ranges);
    }

    public IASString(CharSequence sequence, List<IASTaintRange> ranges) {
        this(sequence.toString());
        this.appendRangesFrom(ranges);
    }

    public static IASString tainted(String str) {
        return new IASString(str, true);
    }

    @Override
    public boolean isTainted() {
        if (this.taintInformation == null) {
            return false;
        }
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
            if (isUninitialized()) {
                this.taintInformation = new IASTaintInformation();
            }
            this.taintInformation.addRange(0, this.str.length(), (short) IASTaintSource.TS_CS_UNKNOWN_ORIGIN.getId());
        }
    }

    public void initialize() {
        if(isUninitialized()) {
            this.taintInformation = new IASTaintInformation();
        }
    }

    private void appendRangesFrom(IASTaintInformation iasTaintInformation) {
        if(iasTaintInformation == null) {
            return;
        }
        appendRangesFrom(iasTaintInformation.getAllRanges());
    }

    private void appendRangesFrom(List<IASTaintRange> ranges) {
        if (isUninitialized() && ranges.size() > 0) {
            this.taintInformation = new IASTaintInformation(ranges);
        } else if (ranges.size() > 0) {
            this.taintInformation.appendRanges(ranges);
        }
    }

    public IASString(String s, IASTaintInformation iasTaintInformation) {
        this(s);
        this.appendRangesFrom(iasTaintInformation);
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
        this.appendRangesFrom(builder.getTaintInformation());
    }

    public IASString(IASStringBuffer buffer) {
        this(buffer.toString());
        this.appendRangesFrom(buffer.getTaintInformation());
    }

    public IASString(IASString string) {
        this(string.str);
        this.appendRangesFrom(string.getTaintInformation());
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
        return this.str.contentEquals(sb);
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
        if (isTainted()) {
            List<IASTaintRange> ranges = this.taintInformation.getRanges(beginIndex, endIndex);
            IASTaintRangeUtils.adjustRanges(ranges, beginIndex, endIndex, beginIndex);
            return ranges;
        } else {
            return new ArrayList<>(0);
        }
    }

    public IASString substring(int beginIndex) {
        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, this.length());
        return new IASString(this.str.substring(beginIndex), ranges);
    }

    public IASString substring(int beginIndex, int endIndex) {
        if (beginIndex < 0 || this.length() < endIndex || endIndex < beginIndex) {
            throw new IllegalArgumentException("startIndex: " + beginIndex + ", endIndex: " + endIndex);
        }

        if (beginIndex == endIndex) {
            return new IASString();
        }

        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, endIndex);
        return new IASString(this.str.substring(beginIndex, endIndex), ranges);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        List<IASTaintRange> ranges = this.getSubstringRanges(beginIndex, endIndex);
        return new IASString(this.str.subSequence(beginIndex, endIndex), ranges);
    }

    public IASString concat(IASString str) {
        IASString newStr = new IASString(this.str.concat(str.str), this.getAllRangesAdjusted());

        List<IASTaintRange> otherRanges = str.getAllRangesAdjusted();
        IASTaintRangeUtils.adjustRanges(otherRanges, 0, str.length(), -this.length());

        newStr.appendRangesFrom(otherRanges);

        return newStr;
    }

    List<IASTaintRange> getAllRanges() {
        return isTainted() ? this.taintInformation.getAllRanges() : new ArrayList<>(0);
    }

    List<IASTaintRange> getAllRangesAdjusted() {
        List<IASTaintRange> ranges = getAllRanges();
        IASTaintRangeUtils.adjustRanges(ranges, 0, this.length(), 0);
        return ranges;
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
        return new IASString(this.str.replace(oldChar, newChar), this.getAllRangesAdjusted());
    }

    public boolean matches(IASString regex) {
        return this.str.matches(regex.str);
    }

    public boolean contains(CharSequence s) {
        return this.str.contains(s);
    }

    public IASString replaceFirst(IASString regex, IASString replacement) {
        String replacedStr = this.str.replaceFirst(regex.str, replacement.str);
        IASString newStr = new IASString(replacedStr, this.getAllRangesAdjusted());

        // Are both Strings tainted? If not, it's irrelevant if one happened for the tainting
        if (this.isTainted() && replacement.isTainted()) {
            Pattern p = Pattern.compile(regex.str);
            Matcher m = p.matcher(this.str);

            if (m.find()) {
                final int start = m.start();
                final int end = m.end();

                if (!newStr.isTainted()) {
                    newStr.taintInformation = new IASTaintInformation();
                }
                newStr.taintInformation.replaceTaintInformation(start, end, replacement.getAllRangesAdjusted(), replacement.length(), true);
            }
        }
        return newStr;
    }

    public IASString replaceAll(IASString regex, IASString replacement) {
        Pattern p = Pattern.compile(regex.toString());
        Matcher m = p.matcher(this.str);

        IASStringBuilder stringBuilder = new IASStringBuilder();
        int start = 0;
        Replacement replacer = Replacement.createReplacement(replacement);
        while (m.find()) {
            int end = m.start();

            IASString first = this.substring(start, end);
            stringBuilder.append(first, true);
            IASString currRepl = replacer.doReplacement(m, this);
            stringBuilder.append(currRepl, true);
            start = m.end();
        }

        if (start < this.length()) {
            IASString last = this.substring(start);
            stringBuilder.append(last, true);
        }

        return stringBuilder.toIASString();
    }

    private static final class Replacement {
        /**
         * Mapping von group name or group index to index in string
         */
        private final Map<Object, Integer> groups;

        /**
         * Replacement string without the group insertions
         */
        private final IASString clearedReplacementString;

        private Replacement(IASString clearedReplacementString, HashMap<Object, Integer> groups) {
            this.clearedReplacementString = clearedReplacementString;
            this.groups = groups;
        }

        public IASString doReplacement(Matcher m, IASString orig) {
            int lastIndex = -1;
            int shift = 0;
            IASStringBuffer stringBuffer = new IASStringBuffer(this.clearedReplacementString);

            for (Object key : this.groups.keySet()) {
                int start;
                int end;
                if (key instanceof String) {
                    start = m.start((String) key);
                    end = m.end((String) key);
                } else if (key instanceof Integer) {
                    start = m.start((Integer) key);
                    end = m.end((Integer) key);
                } else {
                    throw new IllegalStateException("Group map must not contain something else as strinngs and ints");
                }

                IASString insert = orig.substring(start, end);

                int index = groups.get(key);
                if (index < lastIndex) {
                    throw new IllegalStateException("Map not sorted ascending");
                }
                lastIndex = index;

                stringBuffer.insert(index + shift, insert);
                shift += insert.length();
            }
            return stringBuffer.toIASString();
        }

        public static Replacement createReplacement(IASString repl) {
            LinkedHashMap<Object, Integer> groups = new LinkedHashMap<>();

            boolean escaped = false;
            boolean groupParsing = false;
            boolean indexedParsing = false;
            boolean namedParsing = false;

            int groupIndex = -1;
            String groupName = null;

            IASStringBuilder clearedStringBuilder = new IASStringBuilder();

            for (int i = 0; i < repl.length(); i++) {
                char c = repl.charAt(i);

                if (!escaped) {
                    if (groupParsing) {
                        if (indexedParsing) {
                            if (Character.isDigit(c)) {
                                groupIndex = groupIndex * 10 + Character.getNumericValue(c);
                            } else {
                                groupParsing = false;
                                indexedParsing = false;

                                groups.put(groupIndex, clearedStringBuilder.length());

                                // Analyse character again
                                i--;
                                continue;
                            }
                        } else if (namedParsing) {
                            if (isAlphanum(c)) {
                                groupName += c;
                            } else if (c == '}') {
                                if (groupName.isBlank()) {
                                    throw new IllegalStateException("Groupname cannot be empty!");
                                }
                                groups.put(groupName, clearedStringBuilder.length());

                                groupName = null;
                                namedParsing = false;
                                groupParsing = false;
                            }
                        } else {
                            if (Character.isDigit(c)) {
                                indexedParsing = true;
                                groupIndex = Character.getNumericValue(c);
                            } else if (c == '{') {
                                namedParsing = true;
                            } else {
                                throw new IllegalStateException("After $ there mus be a group index or a named capture group name");
                            }
                        }
                    } else {
                        if (c == '\\') {
                            escaped = true;
                        } else if (c == '$') {
                            groupParsing = true;
                        } else {
                            IASString charStr = repl.substring(i, i + 1);
                            clearedStringBuilder.append(charStr);
                        }
                    }
                } else {
                    IASString charStr = repl.substring(i, i + 1);
                    clearedStringBuilder.append(charStr);
                    escaped = false;
                }
            }

            return new Replacement(clearedStringBuilder.toIASString(), groups);
        }

        private static boolean isAlphanum(char c) {
            return Character.isDigit(c) || Character.isLetter(c);
        }
    }

    public IASString replace(CharSequence target, CharSequence replacement) {
        int start = this.str.indexOf(target.toString());
        if (start < 0) {
            return this;
        }
        IASString beginStr = this.substring(0, start);

        int end = start + target.length();
        IASString endStr = this.substring(end).replace(target, replacement);

        return beginStr.concat(IASString.valueOf(replacement)).concat(endStr);
    }

    // TODO: this propagates the taint for the whole string
    public IASString[] split(IASString regex, int limit) {
        this.str.split(regex.toString(), limit);
        Matcher matcher = Pattern.compile(regex.toString()).matcher(this.str);

        ArrayList<IASString> result = new ArrayList<>();
        int start = 0;
        int count = 0;
        while (matcher.find()) {
            if (limit > 0 && count >= limit) {
                break;
            }

            int matchSize = matcher.end() - matcher.start();
            if (count != 0 || matchSize > 0) {
                int end = matcher.start();

                IASString part = this.substring(start, end);
                result.add(part);

            }
            start = matcher.end();
            count++;
        }

        if (start < this.length() || limit < 0) {
            IASString endPart = this.substring(start);
            result.add(endPart);
        } else if (start == 0 && this.length() == 0) {
            result.add(this);
        }

        return result.toArray(new IASString[result.size()]);
        // This implementation ignored that the split regex is removed
//        String[] parts = this.str.split(regex.str);
//        int start = 0;
//
//        IASString[] result = new IASString[parts.length];
//
//        for(String part : parts) {
//            List<IASTaintRange> r = this.getTaintInformation().getRanges(start, part.length());
//            IASTaintRangeUtils.adjustRanges(r, start, start + part.length(), start);
//
//            IASString newStr = new IASString(part, r);
//
//            start += part.length();
//        }
//
//        return result;
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
        return new IASString(this.str.toLowerCase(locale), this.getAllRangesAdjusted());
    }

    public IASString toLowerCase() {
        return new IASString(this.str.toLowerCase(), this.getAllRangesAdjusted());
    }

    public IASString toUpperCase(Locale locale) {
        return new IASString(this.str.toUpperCase(locale), this.getAllRangesAdjusted());
    }

    public IASString toUpperCase() {
        return new IASString(this.str.toUpperCase(), this.getAllRangesAdjusted());
    }

    public IASString trim() {
        String newStr = this.str.trim();
        int start = this.str.indexOf(newStr);
        int end = start + newStr.length();
        return this.substring(start, end);
    }

    /* JDK 11 BEGIN */
    public IASString strip() {
        String newStr = this.str.strip();
        int start = this.str.indexOf(newStr);
        int end = start + newStr.length();
        return this.substring(start, end);
    }

    public IASString stripLeading() {
        String newStr = this.str.stripLeading();
        int start = this.str.indexOf(newStr);
        int end = start + newStr.length();
        return this.substring(start, end);
    }

    public IASString stripTrailing() {
        String newStr = this.str.stripTrailing();
        int start = this.str.indexOf(newStr);
        int end = start + newStr.length();
        return this.substring(start, end);
    }

    public boolean isBlank() {
        return this.str.isBlank();
    }

    public Stream<IASString> lines() {
        return Arrays.stream(this.split(new IASString("\\n")));
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
        return IASString.fromString(String.format(format.toString(), args));
    }


    public static IASString format(Locale l, IASString format, Object... args) {
        // TODO Implement rainting
        return IASString.fromString(String.format(l, format.toString(), args));
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

    //TODO: sound?
    public IASString intern() {
        return this;
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
        return str.str;
    }

    public String getString() {
        return this.str;
    }

    public IASTaintInformation getTaintInformation() {
        return taintInformation;
    }

    public boolean isUninitialized() {
        return this.taintInformation == null;
    }

    public void abortIfTainted() {
        if (this.isTainted()) {
            System.err.printf("String %s is tainted!\nAborting..!\n", this.str);
            System.exit(1);
        }
    }
}
