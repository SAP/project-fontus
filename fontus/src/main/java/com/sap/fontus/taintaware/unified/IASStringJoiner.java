package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.*;


/**
 * A taint-aware version of the {@code StringJoiner} class
 */
public class IASStringJoiner {
    
    StringJoiner joiner;

    List<IASTaintInformationable> taintList;
    IASTaintInformationable prefixTaint;
    IASTaintInformationable delimiterTaint;
    IASTaintInformationable suffixTaint;
    IASTaintInformationable emptyValueTaint;

    public IASStringJoiner(CharSequence delimiter) {
        this(delimiter, "", "");
    }

    public IASStringJoiner(CharSequence delimiter,
                           CharSequence prefix,
                           CharSequence suffix) {
        this.joiner = new StringJoiner(delimiter, prefix, suffix);
        this.taintList = new ArrayList<>();
        this.delimiterTaint = this.getOrCreateTaintInformation(delimiter);
        this.prefixTaint = this.getOrCreateTaintInformation(prefix);
        this.suffixTaint = this.getOrCreateTaintInformation(suffix);

    }

    public IASStringJoiner(StringJoiner joiner) {
        this.joiner = joiner;
        // Leave the taint information as null, as none of the components can be tainted
    }

    public IASStringJoiner setEmptyValue(CharSequence emptyValue) {
        this.joiner.setEmptyValue(emptyValue);
        this.emptyValueTaint = this.getOrCreateTaintInformation(emptyValue);
        return this;
    }

    @Override
    public String toString() {
        return this.joiner.toString();
    }

    public IASString toIASString() {
        return new IASString(this.toString(), this.getTaintInformation());
    }

    private IASTaintInformationable getTaintInformation() {
        if (this.taintList == null) {
            return null;
        }
        // Check for empty value
        if (this.taintList.isEmpty() && this.emptyValueTaint != null) {
            return this.emptyValueTaint.copy();
        }
        // Not empty, append taint ranges
        int length = 0;
        IASTaintInformationable taintInformation = TaintInformationFactory.createTaintInformation(length);
        // Append prefix
        if (this.prefixTaint != null) {
            taintInformation.insertWithShift(length, this.prefixTaint);
            length += taintInformation.getLength();
        }
        for (int i = 0; i < this.taintList.size(); i++) {
            IASTaintInformationable e = this.taintList.get(i);
            if (e != null) {
                taintInformation.insertWithShift(length, e.copy());
                length += e.getLength();
            }
            // Check if we need to add delimiter
            if (i < (this.taintList.size() - 1)) {
                if (this.delimiterTaint != null) {
                    taintInformation.insertWithShift(length, this.delimiterTaint.copy());
                    length += this.delimiterTaint.getLength();
                }
            }
        }
        if (this.suffixTaint != null) {
            taintInformation.insertWithShift(length, this.suffixTaint.copy());
        }
        return taintInformation;
    }

    public IASStringJoiner add(CharSequence newElement) {
        this.joiner.add(newElement);
        if (this.taintList != null) {
            this.taintList.add(this.getOrCreateTaintInformation(newElement));
        }
        return this;
    }

    public IASStringJoiner merge(IASStringJoiner other) {
        this.joiner.merge(other.joiner);
        if (this.taintList != null) {
            this.taintList.add(other.getTaintInformation());
        }
        return this;
    }

    private IASTaintInformationable getOrCreateTaintInformation(CharSequence cs) {
        IASTaintInformationable taintInfo = null;
        if (cs instanceof IASTaintAware) {
            taintInfo = ((IASTaintAware) cs).getTaintInformation();
        }
        if (taintInfo == null) {
            // Create empty (non-null entry) if original info is null or not taint-aware
            taintInfo = TaintInformationFactory.createTaintInformation(cs.length());
        }
        return taintInfo;
    }

    public StringJoiner getStringJoiner() {
        return this.joiner;
    }

    public static IASStringJoiner fromStringJoiner(StringJoiner joiner) {
        return new IASStringJoiner(joiner);
    }

    public int length() {
        return this.joiner.length();
    }

}
