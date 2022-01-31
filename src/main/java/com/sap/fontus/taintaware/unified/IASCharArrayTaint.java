package com.sap.fontus.taintaware.unified;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

// Make this package private
class IASCharArrayTaint {

    // Use a WeakHashMap here so that the entries are removed once the char array goes out of scope
    // Make sure it is synchronized to prevent the keys going out of scope
    private Map<char[], IASTaintInformationable> globalCharArrayTaintMap = Collections.synchronizedMap(new WeakHashMap<>());

    private IASCharArrayTaint() {}

    private static IASCharArrayTaint instance = new IASCharArrayTaint();

    public static IASCharArrayTaint getInstance() {
        return instance;
    }

    public void setTaint(char[] chars, IASTaintInformationable taint) {
        globalCharArrayTaintMap.put(chars, taint);
    }

    public IASTaintInformationable getTaint(char[] chars) {
        return globalCharArrayTaintMap.get(chars);
    }

    public IASTaintInformationable setTaint(IASTaintInformationable taint, int srcBegin, int srcEnd, char dst[], int dstBegin) {
        // Get the subtaint from the original string
        if ((taint == null)  || (dst == null)) {
            return null;
        }
        IASTaintInformationable slice = taint.slice(srcBegin, srcEnd);
        // Shift the taint
        slice.shiftRight(dstBegin);
        // Save in the cache
        setTaint(dst, slice);
        return slice;
    }

    public IASTaintInformationable getTaint(char value[], int offset, int count) {
        IASTaintInformationable taint = getTaint(value);
        if (taint != null) {
            taint = taint.slice(offset, offset + count);
        }
        return taint;
    }

}
