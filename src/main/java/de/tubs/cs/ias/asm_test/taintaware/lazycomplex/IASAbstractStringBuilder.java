package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations.*;
import de.tubs.cs.ias.asm_test.taintaware.shared.*;
import de.tubs.cs.ias.asm_test.taintaware.shared.range.IASTaintRangeStringBuilderable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("Since15")
public abstract class IASAbstractStringBuilder implements IASTaintRangeStringBuilderable, IASLazyAware {
    private final StringBuilder stringBuilder;
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

    public IASAbstractStringBuilder(IASStringBuilderable strb) {
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
    public IASStringBuilderable append(Object obj) {
        IASString string = IASString.valueOf(obj);
        if (obj == null) {
            string = new IASString("null");
        }
        this.derive(new ConcatOperation(string), string.isInitialized());
        this.stringBuilder.append(string.getString());
        return this;
    }

    @Override
    public IASStringBuilderable append(IASStringBuilderable str) {
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(IASStringable str) {
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(CharSequence seq) {
        return this.append((Object) seq);
    }

    @Override
    public IASStringBuilderable append(CharSequence seq, int start, int end) {
        IASString s = IASString.valueOf(seq, start, end);
        return this.append((Object) s);
    }

    @Override
    public IASStringBuilderable append(char[] s, int offset, int len) {
        IASString str = IASString.valueOf(s, offset, len);
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(char[] chars) {
        IASString str = IASString.valueOf(chars);
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(boolean b) {
        IASString str = IASString.valueOf(b);
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(int i) {
        IASString str = IASString.valueOf(i);
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(long lng) {
        IASString str = IASString.valueOf(lng);
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(float f) {
        IASString str = IASString.valueOf(f);
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(char c) {
        IASString str = IASString.valueOf(c);
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable append(double d) {
        IASString str = IASString.valueOf(d);
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable appendCodePoint(int codePoint) {
        IASString str = IASString.valueOf(Character.toChars(codePoint));
        return this.append((Object) str);
    }

    @Override
    public IASStringBuilderable delete(int start, int end) {
        this.derive(new DeleteOperation(start, end), false);
        this.stringBuilder.delete(start, end);
        return this;
    }

    @Override
    public IASStringBuilderable deleteCharAt(int index) {
        return this.delete(index, index + 1);
    }

    @Override
    public IASStringBuilderable replace(int start, int end, IASStringable str) {
        this.derive(new ReplaceOperation(start, end, (IASString) str), ((IASString) str).isInitialized());
        this.stringBuilder.replace(start, end, str.getString());
        return this;
    }

    @Override
    public IASStringBuilderable insert(int index, char[] str, int offset, int len) {
        IASString insertion = IASString.valueOf(str, offset, len);
        this.derive(new InsertOperation(index, insertion), false);
        this.stringBuilder.insert(index, str, offset, len);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, Object obj) {
        IASString insertion = IASString.valueOf(obj);
        this.derive(new InsertOperation(offset, insertion), insertion.isInitialized());
        this.stringBuilder.insert(offset, obj);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, IASStringable str) {
        this.derive(new InsertOperation(offset, (IASString) str), ((IASString) str).isInitialized());
        this.stringBuilder.insert(offset, str.getString());
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, char[] str) {
        IASString insertion = IASString.valueOf(str);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, str);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int dstOffset, CharSequence s) {
        IASString insertion = IASString.valueOf(s);
        this.derive(new InsertOperation(dstOffset, insertion), insertion.isInitialized());
        this.stringBuilder.insert(dstOffset, s);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int dstOffset, CharSequence s, int start, int end) {
        IASString insertion = IASString.valueOf(s, start, end);
        this.derive(new InsertOperation(dstOffset, insertion), insertion.isInitialized());
        this.stringBuilder.insert(dstOffset, s, start, end);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, boolean b) {
        IASString insertion = IASString.valueOf(b);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, b);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, char c) {
        IASString insertion = IASString.valueOf(c);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, c);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, int i) {
        IASString insertion = IASString.valueOf(i);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, i);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, long l) {
        IASString insertion = IASString.valueOf(l);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, l);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, float f) {
        IASString insertion = IASString.valueOf(f);
        this.derive(new InsertOperation(offset, insertion), false);
        this.stringBuilder.insert(offset, f);
        return this;
    }

    @Override
    public IASStringBuilderable insert(int offset, double d) {
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
    public IASStringBuilderable reverse() {
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
        if (taint != this.isTainted()) {
            if (taint) {
                this.derive(new BaseOperation(Collections.singletonList(new IASTaintRange(0, this.length(), (short) IASTaintSource.TS_CS_UNKNOWN_ORIGIN.getId()))), true);
            } else {
                this.derive(new BaseOperation(), false);
            }
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
    public int compareTo(IASStringBuilderable o) {
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
}
