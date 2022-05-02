package com.sap.fontus.taintaware.unified;

import com.sap.fontus.Constants;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.*;

import java.io.Serializable;
import java.util.stream.IntStream;

@SuppressWarnings({"unused", "Since15"})
public abstract class IASAbstractStringBuilder implements Serializable, Comparable<IASAbstractStringBuilder>, Appendable, CharSequence, IASTaintAware {
    protected StringBuilder stringBuilder;
    private IASTaintInformationable taintInformation;

    public IASAbstractStringBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    public IASAbstractStringBuilder(int capacity) {
        this.stringBuilder = new StringBuilder(capacity);
    }

    public IASAbstractStringBuilder(IASString str) {
        this.stringBuilder = new StringBuilder(str.getString());
        if (str.isTainted()) {
            this.taintInformation = str.getTaintInformationCopied();
        }
    }

    public IASAbstractStringBuilder(IASAbstractStringBuilder sb) {
        this.stringBuilder = new StringBuilder(sb.stringBuilder);
        if (sb.isTainted()) {
            this.taintInformation = sb.getTaintInformationCopied();
        }
    }

    public IASAbstractStringBuilder(CharSequence seq) {
        IASString str = IASString.valueOf(seq);
        this.stringBuilder = new StringBuilder(str.length() + 16);
        this.append(str);
    }

    protected void appendShifted(IASTaintInformationable append, int length) {
        if (append == null) {
            if (isInitialized()) {
                this.taintInformation.resize(this.length() + length);
            }
            return;
        }

        if (isUninitialized()) {
            this.taintInformation = TaintInformationFactory.createTaintInformation(this.length());
        }

        this.taintInformation = this.taintInformation.insertWithShift(this.length(), append);
    }


    public void initialize() {
        if (isUninitialized()) {
            this.taintInformation = TaintInformationFactory.createTaintInformation(this.length());
        }
    }


    public void setTaint(boolean taint) {
        this.setTaint(taint ? IASTaintSourceRegistry.MD_CS_UNKNOWN_ORIGIN : null);
    }


    public void setTaint(IASTaintMetadata data) {
        if (data != null) {
            if (!this.isTainted()) {
                if (isUninitialized()) {
                    this.taintInformation = TaintInformationFactory.createTaintInformation(this.length());
                }
                this.taintInformation.addRange(0, this.length(), data);
            }
        } else {
            this.taintInformation = null;
        }
    }

    @Override
    public void setContent(String content, IASTaintInformationable taintInformation) {
        this.stringBuilder = new StringBuilder(content);
        this.taintInformation = taintInformation == null ? null : taintInformation.copy();
    }

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

    public IASAbstractStringBuilder append(IASString str) {
        this.appendShifted(str == null ? null : str.getTaintInformationCopied(), str == null ? 4 : str.length());

        this.stringBuilder.append(str == null ? null : str.getString());
        return this;
    }

    public IASAbstractStringBuilder append(IASAbstractStringBuilder strb) {
        IASString str = IASString.valueOf(strb);
        this.appendShifted(str.getTaintInformationCopied(), str.length());

        this.stringBuilder.append(str.getString());

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

    public IASAbstractStringBuilder append(char[] s, int offset, int len) {
        this.stringBuilder.append(s, offset, len);
        return this;
    }

    public IASAbstractStringBuilder append(char[] str) {
        this.stringBuilder.append(str);
        return this;
    }

    public IASAbstractStringBuilder append(boolean b) {
        this.stringBuilder.append(b);
        return this;
    }

    public IASAbstractStringBuilder append(char c) {
        this.stringBuilder.append(c);
        return this;
    }

    public IASAbstractStringBuilder append(int i) {
        this.stringBuilder.append(i);
        return this;
    }

    public IASAbstractStringBuilder append(long lng) {
        this.stringBuilder.append(lng);
        return this;
    }

    public IASAbstractStringBuilder append(float f) {
        this.stringBuilder.append(f);
        return this;
    }

    public IASAbstractStringBuilder append(double d) {
        this.stringBuilder.append(d);
        return this;
    }

    public IASAbstractStringBuilder appendCodePoint(int codePoint) {
        this.stringBuilder.appendCodePoint(codePoint);
        return this;
    }

    public IASAbstractStringBuilder delete(int start, int end) {
        this.stringBuilder.delete(start, end);
        if (isTainted()) {
            this.taintInformation.deleteWithShift(start, end);
        }
        return this;
    }

    public IASAbstractStringBuilder deleteCharAt(int index) {
        this.stringBuilder.deleteCharAt(index);
        if (isTainted()) {
            this.taintInformation.deleteWithShift(index, index + 1);
        }
        return this;
    }

    public IASAbstractStringBuilder replace(int start, int end, IASString str) {
        this.stringBuilder.replace(start, end, str.toString());
        if (isUninitialized() && str.isTainted()) {
            this.taintInformation = TaintInformationFactory.createTaintInformation(this.length());
        }
        if (this.isTainted() || str.isTainted()) {
            this.taintInformation.replaceTaint(start, end, str.getTaintInformationInitialized().copy());
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

    public IASAbstractStringBuilder insert(int offset, IASString str) {
        if (isUninitialized() && str.isTainted()) {
            this.taintInformation = TaintInformationFactory.createTaintInformation(this.length());
        }
        if (this.isTainted() || str.isTainted()) {
            this.taintInformation.insertWithShift(offset, str.getTaintInformationInitialized().copy());
        }
        this.stringBuilder.insert(offset, str);
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

    public int indexOf(IASString str) {
        return this.stringBuilder.indexOf(str.getString());
    }

    public int indexOf(IASString str, int fromIndex) {
        return this.stringBuilder.indexOf(str.toString(), fromIndex);
    }

    public int lastIndexOf(IASString str) {
        return this.stringBuilder.lastIndexOf(str.toString());
    }

    public int lastIndexOf(IASString str, int fromIndex) {
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
                IASTaintMetadata oldHighTaint = this.taintInformation.getTaint(i);
                IASTaintMetadata oldLowTaint = this.taintInformation.getTaint(i + 1);

                this.taintInformation.clearTaint(i, i + 2);

                if (oldHighTaint != null) {
                    this.taintInformation.setTaint(i + 1, oldHighTaint);
                }
                if (oldLowTaint != null) {
                    this.taintInformation.setTaint(i, oldLowTaint);
                }
            }
        }
    }


    public String toString() {
        return this.stringBuilder.toString();
    }

    public IASString toIASString() {
        return new IASString(this.stringBuilder.toString(), this.taintInformation == null ? null : this.taintInformation.copy());
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
            this.taintInformation.clearTaint(index, index + 1);
        }
    }

    public void ensureCapacity(int minimumCapacity) {
        this.stringBuilder.ensureCapacity(minimumCapacity);
    }

    public void trimToSize() {
        this.stringBuilder.trimToSize();
    }


    public int length() {
        return this.stringBuilder.length();
    }


    public char charAt(int index) {
        return this.stringBuilder.charAt(index);
    }


    public CharSequence subSequence(int start, int end) {
        return this.toIASString().subSequence(start, end);
    }


    public IntStream chars() {
        return this.stringBuilder.chars();
    }


    public IntStream codePoints() {
        return this.stringBuilder.codePoints();
    }


    public int codePointCount(int beginIndex, int endIndex) {
        return this.stringBuilder.codePointCount(beginIndex, endIndex);
    }


    public int codePointAt(int index) {
        return this.stringBuilder.codePointAt(index);
    }


    public int codePointBefore(int index) {
        return this.stringBuilder.codePointBefore(index);
    }


    public int offsetByCodePoints(int index, int codePointOffset) {
        return this.offsetByCodePoints(index, codePointOffset);
    }


    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        this.stringBuilder.getChars(srcBegin, srcEnd, dst, dstBegin);
        IASCharArrayTaint.getInstance().setTaint(this.getTaintInformation(), srcBegin, srcEnd, dst, dstBegin);
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

    @Override
    public boolean isTaintedAt(int index) {
        if (isUninitialized()) {
            return false;
        }

        return this.taintInformation.getTaint(index) != null;
    }

    @Override
    public IASTaintInformationable getTaintInformation() {
        return this.taintInformation;
    }

    @Override
    public IASTaintInformationable getTaintInformationInitialized() {
        if (isUninitialized()) {
            return TaintInformationFactory.createTaintInformation(this.length());
        }
        return this.taintInformation;
    }

    public IASTaintInformationable getTaintInformationCopied() {
        return this.taintInformation == null ? null : this.taintInformation.copy();
    }

    @Override
    public boolean isUninitialized() {
        return !this.isInitialized();
    }

    @Override
    public boolean isInitialized() {
        return this.taintInformation != null;
    }

    public int compareTo(IASAbstractStringBuilder o) {
        if (Constants.JAVA_VERSION < 11) {
            return this.toIASString().compareTo(IASString.valueOf(o));
        } else {
            return this.stringBuilder.compareTo(o.getStringBuilder());
        }
    }
}
