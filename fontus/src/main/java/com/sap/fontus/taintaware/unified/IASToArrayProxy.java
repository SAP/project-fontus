package com.sap.fontus.taintaware.unified;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IASToArrayProxy {

    private IASToArrayProxy() {
    }

    public static Object[] toArray(Collection<?> list, Object[] array) {
        if (array instanceof IASString[]) {
            List<IASString> converted = new ArrayList<>(list.size());
            boolean notToConvert = false;
            for (Object s : list) {
                if (s instanceof IASString) {
                    notToConvert = true;
                    break;
                }
                converted.add(IASString.valueOf(s));
            }
            if (notToConvert) {
                return list.toArray(array);
            } else {
                return converted.toArray(array);
            }
        }
        return list.toArray(array);
    }

    public static Object[] toArray(Collection<?> list) {
        boolean isString = true;
        if (!list.isEmpty()) {
            for (Object e : list) {
                if (!(e instanceof String)) {
                    isString = false;
                    break;
                }
            }
        }
        if (isString) {
            return toArray(list, new IASString[0]);
        }
        return list.toArray();
    }
}
