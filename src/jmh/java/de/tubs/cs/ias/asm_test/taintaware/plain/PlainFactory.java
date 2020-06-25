package de.tubs.cs.ias.asm_test.taintaware.plain;

import de.tubs.cs.ias.asm_test.Factory;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringPool;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PlainFactory implements Factory {
    @Override
    public IASStringable createString(String s) {
        return new PlainString(s);
    }

    @Override
    public IASStringable createString(String s, int taintRangeCount) {
        return new PlainString(s);
    }

    @Override
    public IASStringable createRandomString(int length) {
        return new PlainString(randomString(length));
    }

    @Override
    public IASStringable createRandomString(int length, int taintRangeCount) {
        return new PlainString(randomString(length));
    }

    @SuppressWarnings("Since15")
    static class PlainString implements IASStringable {
        private final String string;

        private PlainString(String s) {
            this.string = s;
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

        @Override
        public IASStringable substring(int beginIndex) {
            return new PlainString(this.string.substring(beginIndex));
        }

        @Override
        public IASStringable substring(int beginIndex, int endIndex) {
            return new PlainString(this.string.substring(beginIndex, endIndex));
        }

        @Override
        public CharSequence subSequence(int beginIndex, int endIndex) {
            return new PlainString(this.string.subSequence(beginIndex, endIndex).toString());
        }

        @Override
        public IASStringable concat(IASStringable str) {
            return new PlainString(this.string.concat(str.getString()));
        }

        @Override
        public IASStringable replace(char oldChar, char newChar) {
            return new PlainString(this.string.replace(oldChar, newChar));
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
            return new PlainString(this.string.replaceFirst(regex.getString(), replacement.getString()));
        }

        @Override
        public IASStringable replaceAll(IASStringable regex, IASStringable replacement) {
            return new PlainString(this.string.replaceAll(regex.getString(), replacement.getString()));
        }

        @Override
        public IASStringable replace(CharSequence target, CharSequence replacement) {
            return new PlainString(this.string.replace(target, replacement));
        }

        @Override
        public IASStringable[] split(IASStringable regex, int limit) {
            String[] strings = this.string.split(regex.getString(), limit);
            PlainString[] ps = new PlainString[strings.length];
            for (int i = 0; i < strings.length; i++) {
                ps[i] = new PlainString(strings[i]);
            }
            return ps;
        }

        @Override
        public IASStringable[] split(IASStringable regex) {
            String[] strings = this.string.split(regex.getString());
            PlainString[] ps = new PlainString[strings.length];
            for (int i = 0; i < strings.length; i++) {
                ps[i] = new PlainString(strings[i]);
            }
            return ps;
        }

        @Override
        public IASStringable toLowerCase(Locale locale) {
            return new PlainString(this.string.toLowerCase(locale));
        }

        @Override
        public IASStringable toLowerCase() {
            return new PlainString(this.string.toLowerCase());
        }

        @Override
        public IASStringable toUpperCase(Locale locale) {
            return new PlainString(this.string.toUpperCase(locale));
        }

        @Override
        public IASStringable toUpperCase() {
            return new PlainString(this.string.toUpperCase());
        }

        @Override
        public IASStringable trim() {
            return new PlainString(this.string.trim());
        }

        @Override
        public IASStringable strip() {
            return new PlainString(this.string.strip());
        }

        @Override
        public IASStringable stripLeading() {
            return new PlainString(this.string.stripLeading());
        }

        @Override
        public IASStringable stripTrailing() {
            return new PlainString(this.string.stripTrailing());
        }

        @Override
        public boolean isBlank() {
            return this.string.isBlank();
        }

        @Override
        public IASStringable repeat(int count) {
            return new PlainString(this.string.repeat(count));
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
        public Stream<? extends IASStringable> lines() {
            return this.string.lines().map((Function<String, IASStringable>) PlainString::new);
        }

        @Override
        public IASTaintSource getTaintFor(int i) {
            return null;
        }

        @Override
        public void setTaint(IASTaintSource source) {

        }

        @Override
        public boolean isTainted() {
            return false;
        }

        @Override
        public void setTaint(boolean taint) {

        }

        @Override
        public int compareTo(IASStringable iasStringable) {
            return this.string.compareTo(iasStringable.getString());
        }
    }
}
