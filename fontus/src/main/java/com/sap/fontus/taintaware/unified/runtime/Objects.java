package com.sap.fontus.taintaware.unified.runtime;

import com.sap.fontus.taintaware.unified.IASString;

public class Objects {
    public static IASString toString(Object o) {
        return IASString.valueOf(o);
    }

    public static IASString toString(Object o, IASString nullDefault) {
        return o != null ? IASString.toStringOf(o) : nullDefault;
    }
}
