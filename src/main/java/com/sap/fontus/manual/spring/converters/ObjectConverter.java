package com.sap.fontus.manual.spring.converters;

import com.sap.fontus.taintaware.shared.IASStringable;

public class ObjectConverter {
    public static Object convertToStringIfPossible(Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof IASStringable) {
            return ((IASStringable) o).getString();
        }

        return o;
    }
}
