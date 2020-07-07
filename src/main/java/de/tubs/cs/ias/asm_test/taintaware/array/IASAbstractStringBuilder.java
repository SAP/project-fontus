package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings({"unused", "Since15"})
public abstract class IASAbstractStringBuilder implements IASStringBuilderable, IASArrayAware {
    protected final StringBuilder stringBuilder;
    protected IASTaintInformation taintInformation;

    public IASAbstractStringBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    public IASAbstractStringBuilder(int capacity) {
        this.stringBuilder = new StringBuilder(capacity);
    }

    public IASAbstractStringBuilder(IASStringable str) {
        this.stringBuilder = new StringBuilder(str.getString());
        if (((IASString) str).isInitialized()) {
            this.taintInformation = ((IASString) str).getTaintInformation().clone();
        }
    }

    public IASAbstractStringBuilder(CharSequence seq) {
        IASString str = IASString.valueOf(seq);
        this.stringBuilder = new StringBuilder(str.length() + 16);
        this.append(str);
    }

    public IASAbstractStringBuilder(IASStringBuilderable s) {
        this.stringBuilder = new StringBuilder(s.getStringBuilder());
        this.taintInformation = new IASTaintInformation(((IASAbstractStringBuilder) s).getTaints());
    }

    public void initialize() {
        if (isUninitialized()) {
            this.taintInformation = new IASTaintInformation(this.length());
        }
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        if (isUninitialized()) {
            return new ArrayList<>(0);
        }
        return TaintConverter.toTaintRanges(this.taintInformation.getTaints());
    }

    @Override
    public boolean isTaintedAt(int index) {
        if (isUninitialized()) {
            return false;
        }
        return this.taintInformation.isTaintedAt(index);
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

    @Override
    public void setTaint(boolean taint) {
        setTaint(taint ? IASTaintSource.TS_CS_UNKNOWN_ORIGIN : null);
    }

    @Override
    public void setTaint(IASTaintSource source) {
        if (source != null) {
            if (!this.isTainted()) {
                this.initialize();
                this.taintInformation.setTaint(0, this.length(), source);
            }
        } else {
            this.taintInformation = null;
        }
    }

    @Override
    public int[] getTaints() {
        if (this.isUninitialized()) {
            return new int[this.length()];
        }
        this.taintInformation.resize(this.length());
        return this.taintInformation.getTaints();
    }

    @Override
    public boolean isTainted() {
        if (isUninitialized()) {
            return false;
        }
        return this.taintInformation.isTainted();
    }

    public IASAbstractStringBuilder append(Object obj) {
        IASString iasString = IASString.valueOf(obj);
        this.append(iasString);
        return this;
    }

    public IASAbstractStringBuilder append(IASStringable str) {
        IASString string = IASString.valueOf(str);
        if (string.isInitialized()) {
            int[] taints = string.getTaints();
            this.initialize();
            this.taintInformation.setTaint(this.length(), taints);
        } else {
            if (!this.isUninitialized()) {
                this.taintInformation.resize(this.length() + string.length());
            }
        }

        this.stringBuilder.append(string.getString());
        return this;
    }

    public IASAbstractStringBuilder append(StringBuffer strb) {
        this.stringBuilder.append(strb);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder append(IASStringBuilderable strb) {
        this.append(strb.toIASString());
        return this;
    }

    public IASAbstractStringBuilder append(CharSequence cs) {
        IASString iasString = IASString.valueOf(cs);
        return this.append(iasString);
    }

    public IASAbstractStringBuilder append(CharSequence s, int start, int end) {
        IASString iasString = IASString.valueOf(s);
        return this.append(iasString.substring(start, end));
    }

    public IASAbstractStringBuilder append(char[] s, int start, int end) {
        this.stringBuilder.append(s, start, end);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder append(char[] str) {
        this.stringBuilder.append(str);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder append(boolean b) {
        this.stringBuilder.append(b);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder append(char c) {
        this.stringBuilder.append(c);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder append(int i) {
        this.stringBuilder.append(i);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder append(long lng) {
        this.stringBuilder.append(lng);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder append(float f) {
        this.stringBuilder.append(f);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder append(double d) {
        this.stringBuilder.append(d);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder appendCodePoint(int codePoint) {
        this.stringBuilder.appendCodePoint(codePoint);
        if (isInitialized()) {
            this.taintInformation.resize(this.length());
        }
        return this;
    }

    public IASAbstractStringBuilder delete(int start, int end) {
        this.stringBuilder.delete(start, end);
        if (isInitialized()) {
            this.taintInformation.removeTaintFor(start, end, true);
        }
        return this;
    }

    public boolean isInitialized() {
        return !isUninitialized();
    }

    public IASAbstractStringBuilder deleteCharAt(int index) {
        this.stringBuilder.deleteCharAt(index);
        if (isTainted()) {
            this.taintInformation.removeTaintFor(index, index + 1, true);
        }
        return this;
    }

    public IASAbstractStringBuilder replace(int start, int end, IASStringable str) {
        this.stringBuilder.replace(start, end, str.toString());
        if (isUninitialized() && str.isTainted()) {
            this.initialize();
        }
        if (this.isTainted() || str.isTainted()) {
            this.taintInformation.replaceTaint(start, end, ((IASString) str).getTaints());
        }
        return this;
    }

    public IASAbstractStringBuilder insert(int index, char[] str, int offset,
                                           int len) {
        IASString iasString = IASString.valueOf(str, offset, len);
        this.insert(index, iasString);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, Object obj) {
        IASString iasString = IASString.valueOf(obj);
        this.insert(offset, iasString);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, IASStringable str) {
        if (isUninitialized() && str.isTainted()) {
            this.initialize();
        }
        if (this.isTainted() || str.isTainted()) {
            this.taintInformation.insertTaint(offset, ((IASString) str).getTaints());
        }
        this.stringBuilder.insert(offset, str.toString());
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, char[] str) {
        this.insert(offset, str, 0, str.length);
        return this;
    }

    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        this.insert(dstOffset, s, 0, s.length());
        return this;
    }

    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s,
                                           int start, int end) {
        IASString iasString = IASString.valueOf(s);
        iasString = iasString.substring(start, end);
        this.insert(dstOffset, iasString);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, boolean b) {
        IASString s = IASString.valueOf(b);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, char c) {
        IASString s = IASString.valueOf(c);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, int i) {
        IASString s = IASString.valueOf(i);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, long l) {
        IASString s = IASString.valueOf(l);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, float f) {
        IASString s = IASString.valueOf(f);
        return this.insert(offset, s);
    }

    public IASAbstractStringBuilder insert(int offset, double d) {
        IASString s = IASString.valueOf(d);
        return this.insert(offset, s);
    }

    public int indexOf(String str) {
        return this.stringBuilder.indexOf(str);
    }

    public int indexOf(IASStringable str) {
        return this.stringBuilder.indexOf(str.getString());
    }

    public int indexOf(IASStringable str, int fromIndex) {
        return this.stringBuilder.indexOf(str.toString(), fromIndex);
    }

    public int lastIndexOf(IASStringable str) {
        return this.stringBuilder.lastIndexOf(str.toString());
    }

    public int lastIndexOf(IASStringable str, int fromIndex) {
        return this.stringBuilder.lastIndexOf(str.toString(), fromIndex);
    }

    public IASAbstractStringBuilder reverse() {
        this.stringBuilder.reverse();
        if (isTainted()) {
            this.taintInformation.reversed();
        }
        handleSurrogatesForReversed();

        return this;
    }

    private void handleSurrogatesForReversed() {
        if (!isTainted()) {
            return;
        }

        char[] chars = this.toString().toCharArray();
        for (int i = 0; i < this.length() - 1; i++) {
            char highSur = chars[i];
            char lowSur = chars[i + 1];
            if (Character.isLowSurrogate(lowSur) && Character.isHighSurrogate(highSur)) {
                this.taintInformation.switchTaint(i, i + 1);
            }
        }
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }

    public IASString toIASString() {
        return new IASString(this.stringBuilder.toString(), this.getTaints());
    }

    public int capacity() {
        return this.stringBuilder.capacity();
    }

    public IASString substring(int start) {
        return this.toIASString().substring(start);
    }

    public IASString substring(int start, int end) {
        return this.toIASString().substring(start, end);
    }

    public void setCharAt(int index, char c) {
        this.stringBuilder.setCharAt(index, c);
        if (isTainted()) {
            this.taintInformation.removeTaintFor(index, index + 1, false);
        }
    }

    public void ensureCapacity(int minimumCapacity) {
        this.stringBuilder.ensureCapacity(minimumCapacity);
    }

    public void trimToSize() {
        this.stringBuilder.trimToSize();
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
        return this.toIASString().subSequence(start, end);
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

    public StringBuilder getStringBuilder() {
        return this.stringBuilder;
    }

    public void setLength(int newLength) {
        this.stringBuilder.setLength(newLength);
        if (isTainted()) {
            this.taintInformation.resize(newLength);
        }
    }

    public IASTaintInformation getTaintInformation() {
        return this.taintInformation;
    }

    public boolean isUninitialized() {
        return this.taintInformation == null;
    }
}
