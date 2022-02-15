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
        joiner = new StringJoiner(delimiter, prefix, suffix);
        taintList = new ArrayList<>();
        delimiterTaint = getOrCreateTaintInformation(delimiter);
        prefixTaint = getOrCreateTaintInformation(prefix);
        suffixTaint = getOrCreateTaintInformation(suffix);

    }

    public IASStringJoiner(StringJoiner joiner) {
        this.joiner = joiner;
        // Leave the taint information as null, as none of the components can be tainted
    }

    public IASStringJoiner setEmptyValue(CharSequence emptyValue) {
        joiner.setEmptyValue(emptyValue);
        emptyValueTaint = getOrCreateTaintInformation(emptyValue);
        return this;
    }

    @Override
    public String toString() {
        return joiner.toString();
    }

    public IASString toIASString() {
        return new IASString(this.toString(), this.getTaintInformation());
    }

    private IASTaintInformationable getTaintInformation() {
        if (taintList == null) {
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
        if (prefixTaint != null) {
            taintInformation.insertWithShift(length, prefixTaint);
            length += taintInformation.getLength();
        }
        for (int i = 0; i < taintList.size(); i++) {
            IASTaintInformationable e = taintList.get(i);
            if (e != null) {
                taintInformation.insertWithShift(length, e.copy());
                length += e.getLength();
            }
            // Check if we need to add delimiter
            if (i < (taintList.size() - 1)) {
                if (delimiterTaint != null) {
                    taintInformation.insertWithShift(length, delimiterTaint.copy());
                    length += delimiterTaint.getLength();
                }
            }
        }
        if (suffixTaint != null) {
            taintInformation.insertWithShift(length, suffixTaint.copy());
        }
        return taintInformation;
    }

    public IASStringJoiner add(CharSequence newElement) {
        joiner.add(newElement);
        if (taintList != null) {
            taintList.add(getOrCreateTaintInformation(newElement));
        }
        return this;
    }

    public IASStringJoiner merge(IASStringJoiner other) {
        joiner.merge(other.getStringJoiner());
        if (taintList != null) {
            taintList.add(other.getTaintInformation());
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
        return joiner.length();
    }

}
