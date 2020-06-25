package de.tubs.cs.ias.asm_test.taintaware.plain;

import de.tubs.cs.ias.asm_test.Factory;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PlainFactory implements Factory {
    @Override
    public IASStringable createString(String s) {
        return null;
    }

    @Override
    public IASStringable createString(String s, int taintRangeCount) {
        return null;
    }

    @Override
    public IASStringable createRandomString(int length) {
        return null;
    }

    @Override
    public IASStringable createRandomString(int length, int taintRangeCount) {
        return null;
    }
//    static class PlainString implements IASStringable {
//        private final String string;
//
//        private PlainString(String s) {
//            this.string = s;
//        }
//
//        @Override
//        public void abortIfTainted() {
//
//        }
//
//        @Override
//        public int length() {
//            return this.string.length();
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return this.string.isEmpty();
//        }
//
//        @Override
//        public char charAt(int index) {
//            return this.string.charAt(index);
//        }
//
//        @Override
//        public int codePointAt(int index) {
//            return this.string.codePointAt(index);
//        }
//
//        @Override
//        public int codePointBefore(int index) {
//            return this.string.codePointBefore(index);
//        }
//
//        @Override
//        public int codePointCount(int beginIndex, int endIndex) {
//            return this.string.codePointCount(beginIndex, endIndex);
//        }
//
//        @Override
//        public int offsetByCodePoints(int index, int codePointOffset) {
//            return this.string.offsetByCodePoints(index, codePointOffset);
//        }
//
//        @Override
//        public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
//            this.string.getChars(srcBegin, srcEnd, dst, dstBegin)
//        }
//
//        @Override
//        public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
//            this.string.getBytes(srcBegin, srcEnd, dst, dstBegin);
//        }
//
//        @Override
//        public byte[] getBytes(IASStringable charsetName) throws UnsupportedEncodingException {
//            return this.string.getBytes(charsetName.getString());
//        }
//
//        @Override
//        public byte[] getBytes(Charset charset) {
//            return this.string.getBytes(charset);
//        }
//
//        @Override
//        public byte[] getBytes() {
//            return this.string.getBytes();
//        }
//
//        @Override
//        public boolean contentEquals(IASStringBuilderable sb) {
//            return this.string.contentEquals(sb.getBuilder());
//        }
//
//        @Override
//        public boolean contentEquals(CharSequence cs) {
//            return this.string.contentEquals(cs);
//        }
//
//        @Override
//        public boolean equalsIgnoreCase(IASStringable anotherString) {
//            return false;
//        }
//
//        @Override
//        public int compareToIgnoreCase(IASStringable str) {
//            return 0;
//        }
//
//        @Override
//        public boolean regionMatches(int toffset, IASStringable other, int ooffset, int len) {
//            return false;
//        }
//
//        @Override
//        public boolean regionMatches(boolean ignoreCase, int toffset, IASStringable other, int ooffset, int len) {
//            return false;
//        }
//
//        @Override
//        public boolean startsWith(IASStringable prefix, int toffset) {
//            return false;
//        }
//
//        @Override
//        public boolean startsWith(IASStringable prefix) {
//            return false;
//        }
//
//        @Override
//        public boolean endsWith(IASStringable suffix) {
//            return false;
//        }
//
//        @Override
//        public int indexOf(int ch) {
//            return 0;
//        }
//
//        @Override
//        public int indexOf(int ch, int fromIndex) {
//            return 0;
//        }
//
//        @Override
//        public int lastIndexOf(int ch) {
//            return 0;
//        }
//
//        @Override
//        public int lastIndexOf(int ch, int fromIndex) {
//            return 0;
//        }
//
//        @Override
//        public int indexOf(IASStringable str) {
//            return 0;
//        }
//
//        @Override
//        public int indexOf(IASStringable str, int fromIndex) {
//            return 0;
//        }
//
//        @Override
//        public int lastIndexOf(IASStringable str) {
//            return 0;
//        }
//
//        @Override
//        public int lastIndexOf(IASStringable str, int fromIndex) {
//            return 0;
//        }
//
//        @Override
//        public IASStringable substring(int beginIndex) {
//            return null;
//        }
//
//        @Override
//        public IASStringable substring(int beginIndex, int endIndex) {
//            return null;
//        }
//
//        @Override
//        public CharSequence subSequence(int beginIndex, int endIndex) {
//            return null;
//        }
//
//        @Override
//        public IASStringable concat(IASStringable str) {
//            return null;
//        }
//
//        @Override
//        public IASStringable replace(char oldChar, char newChar) {
//            return null;
//        }
//
//        @Override
//        public boolean matches(IASStringable regex) {
//            return false;
//        }
//
//        @Override
//        public boolean contains(CharSequence s) {
//            return false;
//        }
//
//        @Override
//        public IASStringable replaceFirst(IASStringable regex, IASStringable replacement) {
//            return null;
//        }
//
//        @Override
//        public IASStringable replaceAll(IASStringable regex, IASStringable replacement) {
//            return null;
//        }
//
//        @Override
//        public IASStringable replace(CharSequence target, CharSequence replacement) {
//            return null;
//        }
//
//        @Override
//        public IASStringable[] split(IASStringable regex, int limit) {
//            return new IASStringable[0];
//        }
//
//        @Override
//        public IASStringable[] split(IASStringable regex) {
//            return new IASStringable[0];
//        }
//
//        @Override
//        public IASStringable toLowerCase(Locale locale) {
//            return null;
//        }
//
//        @Override
//        public IASStringable toLowerCase() {
//            return this.string.toLowerCase();
//        }
//
//        @Override
//        public IASStringable toUpperCase(Locale locale) {
//            return null;
//        }
//
//        @Override
//        public IASStringable toUpperCase() {
//            return this.string.toUpperCase();
//        }
//
//        @Override
//        public IASStringable trim() {
//            return this.string.trim();
//        }
//
//        @Override
//        public IASStringable strip() {
//            return this.string.strip();
//        }
//
//        @Override
//        public IASStringable stripLeading() {
//            return this.string.stripLeading();
//        }
//
//        @Override
//        public IASStringable stripTrailing() {
//            return this.string.stripTrailing();
//        }
//
//        @Override
//        public boolean isBlank() {
//            return this.string.isBlank();
//        }
//
//        @Override
//        public IASStringable repeat(int count) {
//            return null;
//        }
//
//        @Override
//        public IASStringable toIASString() {
//            return this.string.toIASString();
//        }
//
//        @Override
//        public IntStream chars() {
//            return this.string.chars();
//        }
//
//        @Override
//        public IntStream codePoints() {
//            return this.string.codePoints();
//        }
//
//        @Override
//        public char[] toCharArray() {
//            return new char[0];
//        }
//
//        @Override
//        public IASStringable intern() {
//            return this.string.intern();
//        }
//
//        @Override
//        public String getString() {
//            return this.string.getString();
//        }
//
//        @Override
//        public Stream<? extends IASStringable> lines() {
//            return null;
//        }
//
//        @Override
//        public IASTaintSource getTaintFor(int i) {
//            return null;
//        }
//
//        @Override
//        public void setTaint(IASTaintSource source) {
//
//        }
//
//        @Override
//        public boolean isTainted() {
//            return this.string.isTainted();
//        }
//
//        @Override
//        public void setTaint(boolean taint) {
//
//        }
//
//        @Override
//        public int compareTo(IASStringable iasStringable) {
//            return 0;
//        }
//    }

}
