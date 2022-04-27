package com.sap.fontus.manual.spring.converters;

import com.sap.fontus.taintaware.unified.IASString;

public class ObjectConverter {
    public static Object convertToStringIfPossible(Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof IASString) {
            return ((IASString) o).getString();
        }

        return o;
    }
}
