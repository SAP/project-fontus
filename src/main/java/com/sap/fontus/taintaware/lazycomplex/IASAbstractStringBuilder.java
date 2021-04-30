package com.sap.fontus.taintaware.lazycomplex;

import com.sap.fontus.taintaware.lazycomplex.operations.*;
import com.sap.fontus.taintaware.shared.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("Since15")
public abstract class IASAbstractStringBuilder implements IASAbstractStringBuilderable, IASTaintRangeAware {
    private StringBuilder stringBuilder;
    private IASTaintInformation taintInformation;

    public IASAbstractStringBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    public IASAbstractStringBuilder(int capacity) {
        this.stringBuilder = new StringBuilder(capacity);
    }

    public IASAbstractStringBuilder(CharSequence seq) {
        IASString str = IASString.valueOf(seq);
        this.stringBuilder = new StringBuilder(seq);
        this.taintInformation = str.getTaintInformation();
    }

    public IASAbstractStringBuilder(IASStringable string) {
        this.stringBuilder = new StringBuilder(string.getString());
        this.taintInformation = ((IASString) string).getTaintInformation();
    }

    public IASAbstractStringBuilder(IASAbstractStringBuilderable strb) {
        this.stringBuilder = new StringBuilder(strb);
        this.taintInformation = ((IASAbstractStringBuilder) strb).taintInformation;
    }

    @Override
    public boolean isTaintedAt(int index) {
        if (isUninitialized()) {
            return false;
        }
        IASTaintRanges trs = new IASTaintRanges(this.taintInformation.getTaintRanges());
        return trs.isTaintedAt(index);
    }

    @Override
    public void setTaint(IASTaintSource source) {
        if (source == null) {
            this.taintInformation = null;
        } else {
            this.taintInformation = new IASTaintInformation(new BaseOperation(0, this.length(), source));
        }
    }

    @Override
    public void setContent(String content, List<IASTaintRange> taintRanges) {
        this.stringBuilder = new StringBuilder(content);
        this.setTaint(taintRanges);
    }

    @Override
    public void setTaint(List<IASTaintRange> ranges) {
        if (ranges == null || ranges.size() == 0) {
            this.taintInformation = null;
        } else {
            this.taintInformation = new IASTaintInformation(new BaseOperation(ranges));
        }
    }

    @Override
    public void initialize() {
        if (this.isUninitialized()) {
            this.taintInformation = new IASTaintInformation();
        }
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        if (this.taintInformation == null) {
            return new ArrayList<>(0);
        }
        return this.taintInformation.getTaintRanges();
    }

    @Override
    public IASAbstractStringBuilderable append(Object obj) {
        IASString string = IASString.valueOf(obj);
        if (obj == null) {
            string = new IASString("null");
        }
        this.derive(new ConcatOperation(string), string.isInitialized());
        this.stringBuilder.append(string.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(IASAbstractStringBuilderable str) {
        return this.append((Object) str);
    }

    @Override
    public IASAbstractStringBuilderable append(IASStringable str) {
        if (str == null) {
            str = new IASString("null");
        }
        this.derive(new ConcatOperation((IASString) str), ((IASString) str).isInitialized());
        this.stringBuilder.append(str.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(CharSequence seq) {
        IASString s = IASString.valueOf(seq);
        return this.append(s);
    }

    @Override
    public IASAbstractStringBuilderable append(CharSequence seq, int start, int end) {
        IASString s = IASString.valueOf(seq, start, end);
        return this.append(s);
    }

    @Override
    public IASAbstractStringBuilderable append(char[] s, int offset, int len) {
        this.stringBuilder.append(s, offset, len);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(char[] chars) {
        this.stringBuilder.append(chars);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(boolean b) {
        this.stringBuilder.append(b);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(int i) {
        this.stringBuilder.append(i);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(long lng) {
        this.stringBuilder.append(lng);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(float f) {
        this.stringBuilder.append(f);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(char c) {
        this.stringBuilder.append(c);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable append(double d) {
        this.stringBuilder.append(d);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable appendCodePoint(int codePoint) {
        this.stringBuilder.appendCodePoint(codePoint);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable delete(int start, int end) {
        this.derive(new DeleteOperation(start, end), false);
        this.stringBuilder.delete(start, end);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable deleteCharAt(int index) {
        return this.delete(index, index + 1);
    }

    @Override
    public IASAbstractStringBuilderable replace(int start, int end, IASStringable str) {
        this.derive(new ReplaceOperation(start, end, (IASString) str), ((IASString) str).isInitialized());
        this.stringBuilder.replace(start, end, str.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int index, char[] str, int offset, int len) {
        IASString insertion = IASString.valueOf(str, offset, len);
        this.derive(new InsertOperation(index, insertion), false);
        this.stringBuilder.insert(index, str, offset, len);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, Object obj) {
        IASString insertion = IASString.valueOf(obj);
        this.derive(new InsertOperation(offset, insertion), insertion.isInitialized());
        this.stringBuilder.insert(offset, obj);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, IASStringable str) {
        this.derive(new InsertOperation(offset, (IASString) str), ((IASString) str).isInitialized());
        this.stringBuilder.insert(offset, str.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, char[] str) {
        IASString insertion = IASString.valueOf(str);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, str);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int dstOffset, CharSequence s) {
        IASString insertion = IASString.valueOf(s);
        this.derive(new InsertOperation(dstOffset, insertion), insertion.isInitialized());
        this.stringBuilder.insert(dstOffset, s);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int dstOffset, CharSequence s, int start, int end) {
        IASString insertion = IASString.valueOf(s, start, end);
        this.derive(new InsertOperation(dstOffset, insertion), insertion.isInitialized());
        this.stringBuilder.insert(dstOffset, s, start, end);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, boolean b) {
        IASString insertion = IASString.valueOf(b);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, b);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, char c) {
        IASString insertion = IASString.valueOf(c);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, c);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, int i) {
        IASString insertion = IASString.valueOf(i);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, i);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, long l) {
        IASString insertion = IASString.valueOf(l);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, l);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, float f) {
        IASString insertion = IASString.valueOf(f);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, f);
        return this;
    }

    @Override
    public IASAbstractStringBuilderable insert(int offset, double d) {
        IASString insertion = IASString.valueOf(d);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, d);
        return this;
    }

    @Override
    public int indexOf(IASStringable str) {
        return this.stringBuilder.indexOf(str.getString());
    }

    @Override
    public int indexOf(IASStringable str, int fromIndex) {
        return this.stringBuilder.indexOf(str.getString(), fromIndex);
    }

    @Override
    public int lastIndexOf(IASStringable str) {
        return this.stringBuilder.lastIndexOf(str.getString());
    }

    @Override
    public int lastIndexOf(IASStringable str, int fromIndex) {
        return this.stringBuilder.lastIndexOf(str.getString(), fromIndex);
    }

    @Override
    public IASAbstractStringBuilderable reverse() {
        this.derive(new ReverseOperation(), false);
        this.stringBuilder.reverse();
        return this;
    }

    @Override
    public IASString toIASString() {
        return new IASString(this.stringBuilder.toString(), this.taintInformation);
    }

    @Override
    public int capacity() {
        return this.stringBuilder.capacity();
    }

    @Override
    public IASString substring(int start) {
        return this.deriveString(this.stringBuilder.substring(start), new SubstringOperation(start));
    }

    @Override
    public IASString substring(int start, int end) {
        return this.deriveString(this.stringBuilder.substring(start, end), new SubstringOperation(start, end));
    }

    @Override
    public void setCharAt(int index, char c) {
        this.derive(new RemoveTaintOperation(index), false);
        this.stringBuilder.setCharAt(index, c);
    }

    @Override
    public void ensureCapacity(int minimumCapacity) {
        this.stringBuilder.ensureCapacity(minimumCapacity);
    }

    @Override
    public void trimToSize() {
        this.stringBuilder.trimToSize();
    }

    @Override
    public StringBuilder getStringBuilder() {
        return this.stringBuilder;
    }

    @Override
    public void setLength(int newLength) {
        if (newLength < this.length()) {
            this.derive(new SubstringOperation(0, newLength), false);
        }
        this.stringBuilder.setLength(newLength);
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
        if (taint) {
            this.derive(new BaseOperation(Collections.singletonList(new IASTaintRange(0, this.length(), IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN.getId()))), true);
        } else {
            this.derive(new BaseOperation(), false);
        }
    }

    @Override
    public int length() {
        return this.stringBuilder.length();
    }

    @Override
    public char charAt(int index) {
        return this.stringBuilder.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.deriveString(this.stringBuilder.subSequence(start, end).toString(), new SubstringOperation(start, end));
    }

    @Override
    public IntStream chars() {
        return this.stringBuilder.chars();
    }

    @Override
    public IntStream codePoints() {
        return this.stringBuilder.codePoints();
    }

    @Override
    public int codePointCount(int beginIndex, int endIndex) {
        return this.stringBuilder.codePointCount(beginIndex, endIndex);
    }

    @Override
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        this.stringBuilder.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public int codePointAt(int index) {
        return this.stringBuilder.codePointAt(index);
    }

    @Override
    public int codePointBefore(int index) {
        return this.stringBuilder.codePointBefore(index);
    }

    @Override
    public int offsetByCodePoints(int index, int codePointOffset) {
        return this.offsetByCodePoints(index, codePointOffset);
    }

    @Override
    public int compareTo(IASAbstractStringBuilderable o) {
        return this.stringBuilder.compareTo(o.getStringBuilder());
    }

    public boolean isInitialized() {
        return this.taintInformation != null;
    }

    public boolean isUninitialized() {
        return this.taintInformation == null;
    }

    public void derive(IASOperation operation, boolean initializeIfNecessary) {
        if (this.isInitialized()) {
            this.taintInformation = new IASTaintInformation(this.stringBuilder.toString(), this.taintInformation, operation);
        } else if (initializeIfNecessary) {
            this.taintInformation = new IASTaintInformation(this.stringBuilder.toString(), new IASTaintInformation(), operation);
        }
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }

    private IASString deriveString(String newString, IASOperation operation) {
        if (this.isInitialized()) {
            return new IASString(newString, new IASTaintInformation(this.stringBuilder.toString(), this.taintInformation, operation));
        }
        return new IASString(newString);
    }

    @Override
    public IASTaintInformation getTaintInformation() {
        return taintInformation;
    }
}
