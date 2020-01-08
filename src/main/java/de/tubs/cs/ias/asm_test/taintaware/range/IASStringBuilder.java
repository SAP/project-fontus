package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASString;
import de.tubs.cs.ias.asm_test.taintaware.IASStringBuffer;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintInformation;

import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public final class IASStringBuilder implements java.io.Serializable, Comparable<IASStringBuilder>, CharSequence, IASTaintAware {

    private final StringBuilder builder;
    private IASTaintInformation taintInformation;

    @Override
    public boolean isTainted() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void setTaint(boolean taint) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    private void mergeTaint(IASTaintAware str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder(int capacity) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder(IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder(StringBuilder sb) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder(CharSequence seq) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(Object obj) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(StringBuffer strb) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(IASStringBuffer strb) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(CharSequence cs) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(CharSequence s, int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(char[] s, int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(char[] str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(boolean b) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(char c) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(int i) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(long lng) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(float f) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder append(double d) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder appendCodePoint(int codePoint) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder delete(int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder deleteCharAt(int index) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder replace(int start, int end, IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int index, char[] str, int offset,
                                   int len) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, Object obj) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, char[] str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int dstOffset, CharSequence s) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int dstOffset, CharSequence s,
                                   int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, boolean b) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, char c) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, int i) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, long l) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, float f) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder insert(int offset, double d) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public int indexOf(String str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public int indexOf(IASString str, int fromIndex) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public int lastIndexOf(IASString str) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public int lastIndexOf(IASString str, int fromIndex) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASStringBuilder reverse() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public String toString() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString toIASString() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public int capacity()  {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString substring(int start) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASString substring(int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void setCharAt(int index, char c) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void ensureCapacity(int minimumCapacity) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void trimToSize() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public int length() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public char charAt(int index) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    // TODO: unsound
    @Override
    public CharSequence subSequence(int start, int end) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public IntStream chars() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public IntStream codePoints() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public StringBuilder getBuilder() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void setLength(int newLength) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public int compareTo(IASStringBuilder o) {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }

    public IASTaintInformation getTaintInformation() {
        // TODO
        throw new UnsupportedOperationException("Not implemented!");
    }
}
