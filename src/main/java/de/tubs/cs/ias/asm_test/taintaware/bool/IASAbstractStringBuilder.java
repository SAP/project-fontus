package de.tubs.cs.ias.asm_test.taintaware.bool;


import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringBuilderable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;

@SuppressWarnings({"SynchronizedMethod", "ReturnOfThis", "WeakerAccess", "ClassWithTooManyConstructors", "ClassWithTooManyMethods", "Since15"})
public abstract class IASAbstractStringBuilder implements IASStringBuilderable, IASTaintAware {

    // TODO: accessed in both  and unsynchronized methods
    private final StringBuilder builder;
    private boolean tainted = false;

    @Override
    public boolean isTainted() {
        return this.tainted;
    }

    @Override
    public void setTaint(boolean taint) {
        this.tainted = taint;
    }

    private void mergeTaint(IASTaintAware other) {
        this.tainted |= other.isTainted();
    }


    public IASAbstractStringBuilder() {
        this.builder = new StringBuilder();
    }

    public IASAbstractStringBuilder(int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    public IASAbstractStringBuilder(IASString str) {
        this.builder = new StringBuilder(str.length() + 16);
        this.builder.append(str);
        this.mergeTaint(str);
    }

    public IASAbstractStringBuilder(String str) {
        this.builder = new StringBuilder(str.length() + 16);
        this.builder.append(str);
    }


    public IASAbstractStringBuilder(CharSequence seq) {
        this.builder = new StringBuilder(seq.length() + 16);
        this.builder.append(seq);
        if (seq instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) seq;
            this.mergeTaint(ta);
        }
    }

    public IASAbstractStringBuilder(StringBuffer buffer) {
        this.builder = new StringBuilder(buffer); //TODO: do a deep copy? Can something mess us up as this is shared?
        this.tainted = false;
    }

    @Override
    public int length() {
        return this.builder.length();
    }


    public int capacity() {
        return this.builder.capacity();
    }


    public void ensureCapacity(int minimumCapacity) {
        this.builder.ensureCapacity(minimumCapacity);
    }


    public void trimToSize() {
        this.builder.trimToSize();
    }

    @Override
    public StringBuilder getBuilder() {
        return new StringBuilder(this.builder);
    }


    public void setLength(int newLength) {
        this.builder.setLength(newLength);
    }

    @Override
    public char charAt(int index) {
        return this.builder.charAt(index);
    }


    public int codePointAt(int index) {
        return this.builder.codePointAt(index);
    }


    public int codePointBefore(int index) {
        return this.builder.codePointBefore(index);
    }


    public int codePointCount(int beginIndex, int endIndex) {
        return this.builder.codePointCount(beginIndex, endIndex);
    }


    public int offsetByCodePoints(int index, int codePointOffset) {
        return this.builder.offsetByCodePoints(index, codePointOffset);
    }


    public void getChars(int srcBegin, int srcEnd, char[] dst,
                         int dstBegin) {
        this.builder.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public void setCharAt(int index, char ch) {
        this.builder.setCharAt(index, ch);
    }

    public IASAbstractStringBuilder append(Object obj) {
        // TODO: fix?
        this.builder.append(String.valueOf(obj));
        return this;
    }

    public IASAbstractStringBuilder append(IASStringable str) {
        if (str == null) {
            String s = null;
            this.builder.append(s);
            return this;
        }
        this.builder.append(str.toIASString());
        this.mergeTaint(str);
        return this;
    }

    public IASAbstractStringBuilder append(String str) {
        this.builder.append(str);
        return this;
    }

    public IASAbstractStringBuilder append(StringBuffer sb) {
        this.builder.append(sb);
        return this;
    }

    public IASAbstractStringBuilder append(IASAbstractStringBuilder sb) {
        this.builder.append(sb);
        this.mergeTaint(sb);
        return this;
    }

    // TODO: Add the abstract base class
    IASAbstractStringBuilder append(IASStringBuilder asb) {
        this.builder.append(asb);
        this.mergeTaint(asb);
        return this;
    }

    @Override
    public IASAbstractStringBuilder append(CharSequence csq) {
        this.builder.append(csq);
        if (csq instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) csq;
            this.mergeTaint(ta);
        }
        return this;
    }

    @Override
    public IASAbstractStringBuilder append(CharSequence csq, int start, int end) {
        this.builder.append(csq, start, end);
        if (csq instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) csq;
            this.mergeTaint(ta);
        }
        return this;
    }

    public IASAbstractStringBuilder append(char[] str) {
        this.builder.append(str);
        return this;
    }

    public IASAbstractStringBuilder append(char[] str, int offset, int len) {
        this.builder.append(str, offset, len);
        return this;
    }

    public IASAbstractStringBuilder append(boolean b) {
        this.builder.append(b);
        return this;
    }

    @Override
    public IASAbstractStringBuilder append(char c) {
        this.builder.append(c);
        return this;
    }

    public IASAbstractStringBuilder append(int i) {
        this.builder.append(i);
        return this;
    }

    public IASAbstractStringBuilder appendCodePoint(int codePoint) {
        this.builder.appendCodePoint(codePoint);
        return this;
    }


    public IASAbstractStringBuilder append(long lng) {
        this.builder.append(lng);
        return this;
    }

    public IASAbstractStringBuilder append(float f) {
        this.builder.append(f);
        return this;
    }

    public IASAbstractStringBuilder append(double d) {
        this.builder.append(d);
        return this;
    }


    public IASAbstractStringBuilder delete(int start, int end) {
        this.builder.delete(start, end);
        if (this.builder.length() == 0) {
            this.tainted = false;
        }
        return this;
    }


    public IASAbstractStringBuilder deleteCharAt(int index) {
        this.builder.deleteCharAt(index);
        if (this.builder.length() == 0) {
            this.tainted = false;
        }
        return this;
    }


    public IASAbstractStringBuilder replace(int start, int end, IASStringable str) {
        this.builder.replace(start, end, str.getString());
        this.mergeTaint(str);
        return this;
    }

    public IASStringable substring(int start) {
        return this.substring(start, this.builder.length());
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new IASString(this.builder.substring(start, end), this.tainted);
    }


    public IASStringable substring(int start, int end) {
        return new IASString(this.builder.substring(start, end), this.tainted);
    }

    public IASAbstractStringBuilder insert(int index, char[] str, int offset,
                                           int len) {
        this.builder.insert(index, str, offset, len);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, Object obj) {
        this.builder.insert(offset, String.valueOf(obj));
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, IASStringable str) {
        this.builder.insert(offset, str);
        this.mergeTaint(str);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, char[] str) {
        this.builder.insert(offset, str);
        return this;
    }

    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        // after narrowing of s to specific type
        // Ditto for toStringCache clearing
        if (s instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) s;
            this.mergeTaint(ta);
        }
        this.builder.insert(dstOffset, s);
        return this;
    }

    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s,
                                           int start, int end) {
        if (s instanceof IASTaintAware) {
            IASTaintAware ta = (IASTaintAware) s;
            this.mergeTaint(ta);
        }
        this.builder.insert(dstOffset, s, start, end);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, boolean b) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of b to String by super class method
        // Ditto for toStringCache clearing
        this.builder.insert(offset, b);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, char c) {
        this.builder.insert(offset, c);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, int i) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of i to String by super class method
        // Ditto for toStringCache clearing
        this.builder.insert(offset, i);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, long l) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of l to String by super class method
        // Ditto for toStringCache clearing
        this.builder.insert(offset, l);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, float f) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of f to String by super class method
        // Ditto for toStringCache clearing
        this.builder.insert(offset, f);
        return this;
    }

    public IASAbstractStringBuilder insert(int offset, double d) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of d to String by super class method
        // Ditto for toStringCache clearing
        this.builder.insert(offset, d);
        return this;
    }

    public int indexOf(IASStringable str) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        return this.builder.indexOf(str.getString());
    }

    public int indexOf(IASStringable str, int fromIndex) {
        return this.builder.indexOf(str.getString(), fromIndex);
    }

    public int lastIndexOf(IASStringable str) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        return this.lastIndexOf(str, this.builder.length()); //TODO: correct?
    }

    public int lastIndexOf(IASStringable str, int fromIndex) {
        return this.builder.lastIndexOf(str.getString(), fromIndex);
    }

    public IASAbstractStringBuilder reverse() {
        this.builder.reverse();
        return this;
    }

    public IASStringable toIASString() {
        return new IASString(this.builder.toString(), this.tainted);
    }

    public String toString() {
        return this.builder.toString();
    }

    @Override
    public int compareTo(IASStringBuilderable o) {
        if (Constants.JAVA_VERSION < 11) {
            return this.toIASString().compareTo(IASString.valueOf(o));
        } else {
            return this.builder.compareTo(o.getBuilder());
        }
    }
}
