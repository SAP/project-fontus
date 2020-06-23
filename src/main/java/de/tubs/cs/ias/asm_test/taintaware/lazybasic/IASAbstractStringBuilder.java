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
public class IASAbstractStringBuilder implements IASStringBuilderable, IASLazyAware {
    private final StringBuilder builder;
    private IASTaintInformation taintInformation;

    public IASAbstractStringBuilder() {
        this.builder = new StringBuilder();
    }

    @Override
    public int hashCode() {
        return this.builder.hashCode();
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }

    public IASAbstractStringBuilder(int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    public IASAbstractStringBuilder(CharSequence seq) {
        IASString string = IASString.valueOf(seq);
        this.builder = new StringBuilder(string.length() + 16);
        this.append(string);
    }

    public IASAbstractStringBuilder(IASStringable string) {
        this.builder = new StringBuilder(string.length() + 16);
        this.append(IASString.valueOf(string));
    }

    public IASAbstractStringBuilder(IASStringBuilderable strb) {
        this.builder = new StringBuilder(strb.length() + 16);
        this.append(strb);
    }

    public IASAbstractStringBuilder(StringBuffer buffer) {
        this.builder = new StringBuilder(buffer);
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
        this.derive(new InsertLayer(this.builder.length(), this.builder.length() + toAppend.length(), toAppend.getTaintInformation()));
        this.builder.append(toAppend.getString());
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
        return this.append((Object) toAppend);
    }

    @Override
    public IASAbstractStringBuilder append(IASStringBuilderable toAppend) {
        return this.append((Object) toAppend);
    }

    public IASTaintInformation getTaintInformation() {
        return this.taintInformation;
    }

    @Override
    public IASAbstractStringBuilder append(char[] s, int offset, int len) {
        IASString string = IASString.valueOf(s, offset, len);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder append(char[] str) {
        IASString string = IASString.valueOf(str);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder append(boolean b) {
        IASString string = IASString.valueOf(b);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder append(int i) {
        IASString string = IASString.valueOf(i);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder append(CharSequence charSequence) {
        return this.append((Object) charSequence);
    }

    @Override
    public IASAbstractStringBuilder append(CharSequence charSequence, int start, int end) {
        IASString string = IASString.valueOf(charSequence, start, end);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder append(char c) {
        IASString string = IASString.valueOf(c);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder append(long lng) {
        IASString string = IASString.valueOf(lng);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder append(float f) {
        IASString string = IASString.valueOf(f);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder append(double d) {
        IASString string = IASString.valueOf(d);
        return this.append(string);
    }

    @Override
    public IASAbstractStringBuilder appendCodePoint(int codePoint) {
        this.builder.appendCodePoint(codePoint);
        return this;
    }

    @Override
    public IASAbstractStringBuilder delete(int start, int end) {
        this.derive(new DeleteLayer(start, end));
        this.builder.delete(start, end);
        return this;
    }

    @Override
    public IASAbstractStringBuilder deleteCharAt(int index) {
        this.derive(new DeleteLayer(index, index + 1));
        this.builder.deleteCharAt(index);
        return this;
    }

    @Override
    public IASAbstractStringBuilder replace(int start, int end, IASStringable str) {
        this.derive(Arrays.asList(new DeleteLayer(start, end), new InsertLayer(start, start + str.length(), ((IASString) str).getTaintInformation())));
        this.builder.replace(start, end, str.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int index, char[] str, int offset, int len) {
        IASString s = IASString.copyValueOf(str, offset, len);
        this.derive(new InsertLayer(index, index + s.length(), s.getTaintInformation()));
        this.builder.insert(index, str, offset, len);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, Object obj) {
        IASString s = IASString.valueOf(obj);
        this.derive(new InsertLayer(offset, offset + s.length(), s.getTaintInformation()));
        this.builder.insert(offset, obj);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, IASStringable str) {
        this.derive(new InsertLayer(offset, offset + str.length(), ((IASString) str).getTaintInformation()));
        this.builder.insert(offset, str.getString());
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, char[] str) {
        IASString s = IASString.valueOf(str);
        this.derive(new InsertLayer(offset, offset + s.length(), s.getTaintInformation()));
        this.builder.insert(offset, str);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        IASString string = IASString.valueOf(s);
        this.derive(new InsertLayer(dstOffset, dstOffset + string.length(), string.getTaintInformation()));
        this.builder.insert(dstOffset, s);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        IASString string = IASString.valueOf(s, start, end);
        this.derive(new InsertLayer(dstOffset, dstOffset + string.length(), string.getTaintInformation()));
        this.builder.insert(dstOffset, s, start, end);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, boolean b) {
        IASString string = IASString.valueOf(b);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.builder.insert(offset, b);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, char c) {
        IASString string = IASString.valueOf(c);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.builder.insert(offset, c);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, int i) {
        IASString string = IASString.valueOf(i);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.builder.insert(offset, i);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, long l) {
        IASString string = IASString.valueOf(l);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.builder.insert(offset, l);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, float f) {
        IASString string = IASString.valueOf(f);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.builder.insert(offset, f);
        return this;
    }

    @Override
    public IASAbstractStringBuilder insert(int offset, double d) {
        IASString string = IASString.valueOf(d);
        this.derive(new InsertLayer(offset, offset + string.length(), string.getTaintInformation()));
        this.builder.insert(offset, d);
        return this;
    }

    @Override
    public int indexOf(IASStringable str) {
        return this.builder.indexOf(str.getString());
    }

    @Override
    public int indexOf(IASStringable str, int fromIndex) {
        return this.builder.indexOf(str.getString(), fromIndex);
    }

    @Override
    public int lastIndexOf(IASStringable str) {
        return this.builder.lastIndexOf(str.getString());
    }

    @Override
    public int lastIndexOf(IASStringable str, int fromIndex) {
        return this.builder.lastIndexOf(str.getString(), fromIndex);
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
        this.builder.reverse();
        return this;
    }

    @Override
    public IASString toIASString() {
        return new IASString(this.builder.toString(), this.taintInformation);
    }

    @Override
    public int capacity() {
        return this.builder.capacity();
    }

    @Override
    public IASString substring(int start) {
        String newStr = this.builder.substring(start);
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
        String newStr = this.builder.substring(start, end);
        return this.deriveString(newStr, Arrays.asList(new DeleteLayer(end), new DeleteLayer(0, start)));
    }

    @Override
    public void setCharAt(int index, char c) {
        this.builder.setCharAt(index, c);
    }

    @Override
    public void ensureCapacity(int minimumCapacity) {
        this.builder.ensureCapacity(minimumCapacity);
    }

    @Override
    public void trimToSize() {
        this.builder.trimToSize();
    }

    @Override
    public StringBuilder getBuilder() {
        return this.builder;
    }

    @Override
    public void setLength(int newLength) {
        if (newLength < this.length()) {
            this.derive(new DeleteLayer(newLength));
        }
        this.builder.setLength(newLength);
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
    public boolean isTainted() {
        if (this.taintInformation == null) {
            return false;
        }
        return this.taintInformation.isTainted();
    }

    @Override
    public void setTaint(boolean taint) {
        this.setTaint(taint ? IASTaintSource.TS_CS_UNKNOWN_ORIGIN : null);
    }

    @Override
    public int length() {
        return this.builder.length();
    }

    @Override
    public char charAt(int i) {
        return this.builder.charAt(i);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.substring(start, end);
    }

    @Override
    public IntStream chars() {
        return this.builder.chars();
    }

    @Override
    public IntStream codePoints() {
        return this.builder.codePoints();
    }

    @Override
    public int compareTo(IASStringBuilderable iasStringBuilderable) {
        return this.builder.compareTo(iasStringBuilderable.getBuilder());
    }
}
