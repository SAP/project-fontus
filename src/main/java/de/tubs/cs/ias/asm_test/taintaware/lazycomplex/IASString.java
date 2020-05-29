package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations.*;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringPool;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class IASString implements IASStringable, IASLazyComplexAware, Comparable<IASString> {
    private final String string;
    private final IASTaintInformation taintInformation;

    public IASString() {
        this.string = "";
        this.taintInformation = new IASTaintInformation(new BaseOperation());
    }

    public IASString(String string) {
        this.string = string;
        this.taintInformation = new IASTaintInformation(new BaseOperation());
    }

    public IASString(String string, IASTaintInformation taintInformation) {
        this.string = string;
        this.taintInformation = taintInformation;
    }

    public IASString(String string, IASOperation operation) {
        this(string, new IASTaintInformation(operation));
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        return this.taintInformation.evaluate();
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
        return;
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
        return;
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
    public IASStringable substring(int beginIndex) {
        String substringed = this.string.substring(beginIndex);
        return new IASString(substringed, new SubstringOperation(this, beginIndex));
    }

    @Override
    public IASStringable substring(int beginIndex, int endIndex) {
        String substringed = this.string.substring(beginIndex, endIndex);
        return new IASString(substringed, new SubstringOperation(this, beginIndex, endIndex));
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        String substringed = this.string.substring(beginIndex, endIndex);
        return new IASString(substringed, new SubstringOperation(this, beginIndex, endIndex));
    }

    @Override
    public IASStringable concat(IASStringable str) {
        String substringed = this.string.concat(str.getString());
        return new IASString(substringed, new ConcatOperation(this, (IASLazyComplexAware) str));
    }

    @Override
    public IASStringable replace(char oldChar, char newChar) {
        return;
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
    public IASStringable replaceFirst(IASStringable regex, IASStringable replacement) {
        String replaced = this.string.replaceFirst(regex.getString(), replacement.getString());
        return new IASString(replaced, new ReplaceFirstOperation(this, (IASString) regex, (IASString) replacement));
    }

    @Override
    public IASStringable replaceAll(IASStringable regex, IASStringable replacement) {
        String replaced = this.string.replaceAll(regex.getString(), replacement.getString());
        return new IASString(replaced, new ReplaceAllOperation(this, (IASString) regex, (IASString) replacement));
    }

    @Override
    public IASStringable replace(CharSequence target, CharSequence replacement) {
        return new IASString(this.string.replace(target, replacement), new ReplaceCharSequenceOperation(this, new IASString(target), new IASString(replacement)));
    }

    @Override
    public IASStringable[] split(IASStringable regex, int limit) {
        String[] splitted = this.string.split(regex.getString(), limit);
        IASString[] strings = new IASString[splitted.length];
        for (int i = 0; i < splitted.length; i++) {
            strings[i] = new IASString(splitted[i], new SplitOperation(this, regex.getString(), i));
        }
        return strings;
    }

    @Override
    public IASStringable[] split(IASStringable regex) {
        String[] splitted = this.string.split(regex.getString());
        IASString[] strings = new IASString[splitted.length];
        for (int i = 0; i < splitted.length; i++) {
            strings[i] = new IASString(splitted[i], new SplitOperation(this, regex.getString(), i));
        }
        return strings;
    }

    @Override
    public IASStringable toLowerCase(Locale locale) {
        return new IASString(this.string.toLowerCase(locale), this.taintInformation);
    }

    @Override
    public IASStringable toLowerCase() {
        return new IASString(this.string.toLowerCase(), this.taintInformation);
    }

    @Override
    public IASStringable toUpperCase(Locale locale) {
        return new IASString(this.string.toUpperCase(locale), this.taintInformation);
    }

    @Override
    public IASStringable toUpperCase() {
        return new IASString(this.string.toUpperCase(), this.taintInformation);
    }

    @Override
    public IASStringable trim() {
        String trimmed = this.string.trim();
        return new IASString(trimmed, new TrimOperation(this));
    }

    @Override
    public IASStringable strip() {
        return new IASString(this.string.strip(), new StripOperation(this, true, true));
    }

    @Override
    public IASStringable stripLeading() {
        return new IASString(this.string.stripLeading(), new StripOperation(this, true, false));
    }

    @Override
    public IASStringable stripTrailing() {
        return new IASString(this.string.stripTrailing(), new StripOperation(this, false, true));
    }

    @Override
    public boolean isBlank() {
        return this.string.isBlank();
    }

    @Override
    public IASStringable repeat(int count) {
        return new IASString(this.string.repeat(count), new RepeatOperation(this, count));
    }

    @Override
    public IASStringable toIASString() {
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
    public IASStringable intern() {
        return IASStringPool.intern(this);
    }

    @Override
    public String getString() {
        return this.string;
    }

    @Override
    public boolean isTainted() {
        return this.taintInformation.isTainted();
    }

    @Override
    public void setTaint(boolean taint) {
        // TODO
    }

    @Override
    public int compareTo(IASString o) {
        return this.string.compareTo(o.getString());
    }
}
