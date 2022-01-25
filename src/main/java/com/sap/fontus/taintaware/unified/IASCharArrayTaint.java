package com.sap.fontus.taintaware.unified;

import java.util.Map;
import java.util.WeakHashMap;

public class IASCharArrayTaint {

    private Map<char[], IASTaintInformationable> globalCharArrayTaintMap = new WeakHashMap<>();


    private IASCharArrayTaint() {}

    private static IASCharArrayTaint instance = new IASCharArrayTaint();

    public static IASCharArrayTaint getInstance() {
        return instance;
    }

    private void saveCharTaint(char[] chars, IASTaintInformationable taint) {
        globalCharArrayTaintMap.put(chars, taint);
    }

    private IASTaintInformationable getCharTaint(char[] chars) {
        return globalCharArrayTaintMap.get(chars);
    }

    public IASTaintInformationable setTaint(IASTaintInformationable taint, int srcBegin, int srcEnd, char dst[], int dstBegin) {
        // Get the subtaint from the original string
        IASTaintInformationable slice = taint.slice(srcBegin, srcEnd);
        // Shift the taint
        slice.shiftRight(dstBegin);
        // Save in the cache
        saveCharTaint(dst, slice);
        return slice;
    }

    public IASTaintInformationable getTaint(char value[], int offset, int count) {
        IASTaintInformationable taint = getCharTaint(value);
        if (taint != null) {
            taint = taint.slice(offset, offset + count);
        }
        return taint;
    }

    public IASTaintInformationable getTaint(char value[]) {
        return getCharTaint(value);
    }

}
