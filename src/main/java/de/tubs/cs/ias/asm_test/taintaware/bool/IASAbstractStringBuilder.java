package de.tubs.cs.ias.asm_test.taintaware.bool;


import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASAbstractStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"SynchronizedMethod", "ReturnOfThis", "WeakerAccess", "ClassWithTooManyConstructors", "ClassWithTooManyMethods", "Since15"})
public abstract class IASAbstractStringBuilder implements IASAbstractStringBuilderable, IASTaintAware {

    // TODO: accessed in both  and unsynchronized methods
    private final StringBuilder stringBuilder;
    private boolean tainted = false;

    @Override
    public boolean isTainted() {
        return this.tainted;
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        if (isTainted()) {
            return Collections.singletonList(new IASTaintRange(0, this.length(), IASTaintSource.TS_CS_UNKNOWN_ORIGIN));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void setTaint(boolean taint) {
        if (this.stringBuilder.length() > 0 || !taint) {
            this.tainted = taint;
        }
    }

    @Override
    public void setTaint(IASTaintSource source) {
        if (this.stringBuilder.length() > 0 || source == null) {
            this.tainted = source != null;
        }
    }

    private void mergeTaint(IASTaintAware other) {
        if (other != null) {
            this.tainted |= other.isTainted();
        }
    }


    public IASAbstractStringBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    public IASAbstractStringBuilder(int capacity) {
        this.stringBuilder = new StringBuilder(capacity);
    }

    public IASAbstractStringBuilder(IASStringable str) {
        this.stringBuilder = new StringBuilder(str.getString());
        this.mergeTaint(str);
    }

    public IASAbstractStringBuilder(String str) {
        this.stringBuilder = new StringBuilder(str.length() + 16);
        this.stringBuilder.append(str);
    }


    public IASAbstractStringBuilder(CharSequence seq) {
        this.stringBuilder = new StringBuilder(seq.length() + 16);
        this.stringBuilder.append(seq);
        if (seq instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) seq;
            this.mergeTaint(ta);
        }
    }

    public IASAbstractStringBuilder(StringBuffer buffer) {
        this.stringBuilder = new StringBuilder(buffer); //TODO: do a deep copy? Can something mess us up as this is shared?
        this.tainted = false;
    }

    @Override
    public int length() {
        return this.stringBuilder.length();
    }


    public int capacity() {
        return this.stringBuilder.capacity();
    }


    public void ensureCapacity(int minimumCapacity) {
        this.stringBuilder.ensureCapacity(minimumCapacity);
    }


    public void trimToSize() {
        this.stringBuilder.trimToSize();
    }

    @Override
    public StringBuilder getStringBuilder() {
        return new StringBuilder(this.stringBuilder);
    }


    public void setLength(int newLength) {
        this.stringBuilder.setLength(newLength);
    }

    @Override
    public char charAt(int index) {
        return this.stringBuilder.charAt(index);
    }


    public int codePointAt(int index) {
        return this.stringBuilder.codePointAt(index);
    }


    public int codePointBefore(int index) {
        return this.stringBuilder.codePointBefore(index);
    }


    public int codePointCount(int beginIndex, int endIndex) {
        return this.stringBuilder.codePointCount(beginIndex, endIndex);
    }


    public int offsetByCodePoints(int index, int codePointOffset) {
        return this.stringBuilder.offsetByCodePoints(index, codePointOffset);
    }


    public void getChars(int srcBegin, int srcEnd, char[] dst,
                         int dstBegin) {
        this.stringBuilder.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public void setCharAt(int index, char ch) {
        this.stringBuilder.setCharAt(index, ch);
    }

    public IASAbstractStringBuilder append(Object obj) {
        // TODO: fix?
        this.stringBuilder.append(obj);
        return this;
    }

    public IASAbstractStringBuilder append(IASStringable str) {
        if (str == null) {
            String s = null;
            this.stringBuilder.append(s);
            return this;
        }
        this.stringBuilder.append(str.toIASString());
        this.mergeTaint(str);
        return this;
    }

    public IASAbstractStringBuilder append(String str) {
        this.stringBuilder.append(str);
        return this;
    }

    public IASAbstractStringBuilder append(IASAbstractStringBuilderable sb) {
        this.stringBuilder.append(sb.getStringBuilder());
        this.mergeTaint(sb);
        return this;
    }

    public IASAbstractStringBuilder append(IASAbstractStringBuilder sb) {
        this.stringBuilder.append(sb.getStringBuilder());
        this.mergeTaint(sb);
        return this;
    }

    @Override
    public IASAbstractStringBuilder append(CharSequence csq) {
        this.stringBuilder.append(csq);
        if (csq instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) csq;
            this.mergeTaint(ta);
        }
        return this;
    }

    @Override
    public IASAbstractStringBuilder append(CharSequence csq, int start, int end) {
        this.stringBuilder.append(csq, start, end);
        if (csq instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) csq;
            this.mergeTaint(ta);
        }
        return this;
    }

    public IASAbstractStringBuilder append(char[] str) {
        this.stringBuilder.append(str);
        return this;
    }

    public IASAbstractStringBuilder append(char[] str, int offset, int len) {
        this.stringBuilder.append(str, offset, len);
        return this;
    }

    public IASAbstractStringBuilder append(boolean b) {
        this.stringBuilder.append(b);
        return this;
    }

    @Override
    public IASAbstractStringBuilder append(char c) {
        this.stringBuilder.append(c);
        return this;
    }

    public IASAbstractStringBuilder append(int i) {
        this.stringBuilder.append(i);
        return this;
    }

    public IASAbstractStringBuilder appendCodePoint(int codePoint) {
        this.stringBuilder.appendCodePoint(codePoint);
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


    public IASAbstractStringBuilder delete(int start, int end) {
        this.stringBuilder.delete(start, end);
        if (this.stringBuilder.length() == 0) {
            this.tainted = false;
        }
        return this;
    }


    public IASAbstractStringBuilder deleteCharAt(int index) {
        this.stringBuilder.deleteCharAt(index);
        if (this.stringBuilder.length() == 0) {
            this.tainted = false;
        }
        return this;
    }


    public IASAbstractStringBuilder replace(int start, int end, IASStringable str) {
        this.stringBuilder.replace(start, end, str.getString());
        this.mergeTaint(str);
        return this;
    }

    public IASString substring(int start) {
        return this.substring(start, this.stringBuilder.length());
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new IASString(this.stringBuilder.substring(start, end), this.tainted);
    }


    public IASString substring(int start, int end) {
        return new IASString(this.stringBuilder.substring(start, end), this.tainted);
    }

    public IASAbstractStringBuilder insert(int index, char[] str, int offset,
                                           int len) {
        this.stringBuilder.insert(index, str, offset, len);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, Object obj) {
        return this.insert(offset, IASString.valueOf(obj));
    }

    public IASAbstractStringBuilder insert(int offset, IASStringable str) {
        this.stringBuilder.insert(offset, str);
        this.mergeTaint(str);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, char[] str) {
        this.stringBuilder.insert(offset, str);
        return this;
    }

    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        if (s instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) s;
            this.mergeTaint(ta);
        }
        this.stringBuilder.insert(dstOffset, s);
        return this;
    }

    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s,
                                           int start, int end) {
        if (s instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) s;
            this.mergeTaint(ta);
        }
        this.stringBuilder.insert(dstOffset, s, start, end);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, boolean b) {
        this.stringBuilder.insert(offset, b);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, char c) {
        this.stringBuilder.insert(offset, c);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, int i) {
        this.stringBuilder.insert(offset, i);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, long l) {
        this.stringBuilder.insert(offset, l);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, float f) {
        this.stringBuilder.insert(offset, f);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, double d) {
        this.stringBuilder.insert(offset, d);
        return this;
    }

    public int indexOf(IASStringable str) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        return this.stringBuilder.indexOf(str.getString());
    }

    public int indexOf(IASStringable str, int fromIndex) {
        return this.stringBuilder.indexOf(str.getString(), fromIndex);
    }

    public int lastIndexOf(IASStringable str) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        return this.lastIndexOf(str, this.stringBuilder.length()); //TODO: correct?
    }

    public int lastIndexOf(IASStringable str, int fromIndex) {
        return this.stringBuilder.lastIndexOf(str.getString(), fromIndex);
    }

    public IASAbstractStringBuilder reverse() {
        this.stringBuilder.reverse();
        return this;
    }

    public IASString toIASString() {
        return new IASString(this.stringBuilder.toString(), this.tainted);
    }

    public String toString() {
        return this.stringBuilder.toString();
    }

    @Override
    public int compareTo(IASAbstractStringBuilderable o) {
        if (Constants.JAVA_VERSION < 11) {
            return this.toIASString().compareTo(IASString.valueOf(o));
        } else {
            return this.stringBuilder.compareTo(o.getStringBuilder());
        }
    }
}
