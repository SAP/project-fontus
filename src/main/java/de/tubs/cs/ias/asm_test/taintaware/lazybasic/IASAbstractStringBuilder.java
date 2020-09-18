package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation.BaseLayer;
import de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation.DeleteLayer;
import de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation.InsertLayer;
import de.tubs.cs.ias.asm_test.taintaware.shared.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("Since15")
public class IASAbstractStringBuilder implements IASAbstractStringBuilderable, IASTaintRangeAware {
    private final StringBuilder stringBuilder;
    private IASTaintInformation taintInformation;

    public IASAbstractStringBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public int hashCode() {
        return this.stringBuilder.hashCode();
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }

    public IASAbstractStringBuilder(int capacity) {
        this.stringBuilder = new StringBuilder(capacity);
    }

    public IASAbstractStringBuilder(CharSequence seq) {
        IASString string = IASString.valueOf(seq);
        this.stringBuilder = new StringBuilder(string.length() + 16);
        this.append(string);
        this.ensureCapacity(string.length() + 16);
    }

    public IASAbstractStringBuilder(IASStringable string) {
        this.stringBuilder = new StringBuilder(string.length() + 16);
        this.append(IASString.valueOf(string));
    }

    public IASAbstractStringBuilder(IASAbstractStringBuilderable strb) {
        this.stringBuilder = new StringBuilder(strb.length() + 16);
        this.append(strb);
    }

    public IASAbstractStringBuilder(StringBuffer buffer) {
        this.stringBuilder = new StringBuilder(buffer);
    }

    public IASAbstractStringBuilder(IASString string) {
        this((IASStringable) string);
    }

    @Override
    public IASAbstractStringBuilder append(Object obj) {
        IASString toAppend = IASString.valueOf(obj);
        if (obj == null) {
            toAppend = new IASString("null");
        }
        this.derive(new InsertLayer(this.stringBuilder.length(), this.stringBuilder.length() + toAppend.length(), toAppend.getTaintInformation()));
        this.stringBuilder.append(toAppend.getString());
        return this;
    }

    private void derive(IASLayer layer) {
        this.derive(Collections.singletonList(layer));
    }

    private void derive(List<IASLayer> layers) {
        this.taintInformation = new IASTaintInformation(layers, this.taintInformation);
    }

    @Override
    public IASAbstractStringBuilder append(IASStringable toAppend) {
        if (toAppend == null) {
            toAppend = new IASString("null");
        }
        this.derive(new InsertLayer(this.stringBuilder.length(), this.stringBuilder.length() + toAppend.length(), ((IASString) toAppend).getTaintInformation()));
        this.stringBuilder.append(toAppend.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilder append(IASAbstractStringBuilderable toAppend) {
        return this.append((Object) toAppend);
    }

    public IASTaintInformation getTaintInformation() {
        return this.taintInformation;
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
    public IASAbstractStringBuilder delete(int start, int end) {
        this.derive(new DeleteLayer(start, end));
        this.stringBuilder.delete(start, end);
        return this;
    }

    @Override
    public IASAbstractStringBuilder deleteCharAt(int index) {
        this.derive(new DeleteLayer(index, index + 1));
        this.stringBuilder.deleteCharAt(index);
        return this;
    }

    @Override
    public IASAbstractStringBuilder replace(int start, int end, IASStringable str) {
        this.derive(Arrays.asList(new DeleteLayer(start, end), new InsertLayer(start, start + str.length(), ((IASString) str).getTaintInformation())));
        this.stringBuilder.replace(start, end, str.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int index, char[] str, int offset, int len) {
        IASString s = IASString.copyValueOf(str, offset, len);
        this.derive(new InsertLayer(index, index + s.length(), s.getTaintInformation()));
        this.stringBuilder.insert(index, str, offset, len);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, Object obj) {
        IASString s = IASString.valueOf(obj);
        this.derive(new InsertLayer(offset, offset + s.length(), s.getTaintInformation()));
        this.stringBuilder.insert(offset, obj);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, IASStringable str) {
        this.derive(new InsertLayer(offset, offset + str.length(), ((IASString) str).getTaintInformation()));
        this.stringBuilder.insert(offset, str.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, char[] str) {
        IASString s = IASString.valueOf(str);
        this.derive(new InsertLayer(offset, offset + s.length(), s.getTaintInformation()));
        this.stringBuilder.insert(offset, str);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        IASString string = IASString.valueOf(s);
        this.derive(new InsertLayer(dstOffset, dstOffset + string.length(), string.getTaintInformation()));
        this.stringBuilder.insert(dstOffset, s);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        IASString string = IASString.valueOf(s, start, end);
        this.derive(new InsertLayer(dstOffset, dstOffset + string.length(), string.getTaintInformation()));
        this.stringBuilder.insert(dstOffset, s, start, end);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, boolean b) {
        IASString string = IASString.valueOf(b);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.stringBuilder.insert(offset, b);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, char c) {
        IASString string = IASString.valueOf(c);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.stringBuilder.insert(offset, c);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, int i) {
        IASString string = IASString.valueOf(i);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.stringBuilder.insert(offset, i);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, long l) {
        IASString string = IASString.valueOf(l);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.stringBuilder.insert(offset, l);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, float f) {
        IASString string = IASString.valueOf(f);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.stringBuilder.insert(offset, f);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, double d) {
        IASString string = IASString.valueOf(d);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
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
    public IASAbstractStringBuilder reverse() {
        // TODO Handle surrogates
        List<IASLayer> layers = new ArrayList<>();
        layers.add(new DeleteLayer());
        for (int i = 0; i < this.length(); i++) {
            int swap = this.length() - i - 1;
            IASTaintInformation other = new IASTaintInformation(Arrays.asList(
                    new DeleteLayer(swap + 1),
                    new DeleteLayer(0, swap)
            ), this.taintInformation);

            InsertLayer layer = new InsertLayer(i, i + 1, other);
            layers.add(layer);
        }
        this.derive(layers);
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
        String newStr = this.stringBuilder.substring(start);
        return this.deriveString(newStr, new DeleteLayer(0, start));
    }

    private IASString deriveString(String newStr, IASLayer layer) {
        return this.deriveString(newStr, Collections.singletonList(layer));
    }

    private IASString deriveString(String newStr, List<IASLayer> layers) {
        return new IASString(newStr, new IASTaintInformation(layers, this.taintInformation));
    }

    @Override
    public IASString substring(int start, int end) {
        String newStr = this.stringBuilder.substring(start, end);
        return this.deriveString(newStr, Arrays.asList(new DeleteLayer(end), new DeleteLayer(0, start)));
    }

    @Override
    public void setCharAt(int index, char c) {
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
            this.derive(new DeleteLayer(newLength));
        }
        this.stringBuilder.setLength(newLength);
    }

    @Override
    public List<IASTaintRange> getTaintRanges() {
        if (this.isUninitialized()) {
            return new ArrayList<>(0);
        }
        return this.taintInformation.getTaintRanges();
    }

    @Override
    public boolean isUninitialized() {
        return this.taintInformation == null;
    }

    @Override
    public void initialize() {
        this.taintInformation = new IASTaintInformation();
    }

    @Override
    public boolean isTaintedAt(int index) {
        if (this.isUninitialized()) {
            return false;
        }
        return this.taintInformation.isTaintedAt(index);
    }

    @Override
    public void setTaint(IASTaintSource source) {
        this.taintInformation = null;
        if (this.length() > 0 && source != null) {
            this.taintInformation = new IASTaintInformation(new BaseLayer(0, this.length(), source));
        }
    }


    @Override
    public void setTaint(List<IASTaintRange> ranges) {
        if (ranges == null || ranges.size() == 0) {
            this.taintInformation = null;
        } else {
            this.taintInformation = new IASTaintInformation(new BaseLayer(ranges));
        }
    }

    @Override
    public boolean isTainted() {
        if (this.taintInformation == null) {
            return false;
        }
        return this.taintInformation.isTainted();
    }

    @Override
    public void setTaint(boolean taint) {
        this.setTaint(taint ? IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN : null);
    }

    @Override
    public int length() {
        return this.stringBuilder.length();
    }

    @Override
    public char charAt(int i) {
        return this.stringBuilder.charAt(i);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.substring(start, end);
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
    public int compareTo(IASAbstractStringBuilderable iasStringBuilderable) {
        return this.stringBuilder.compareTo(iasStringBuilderable.getStringBuilder());
    }
}
