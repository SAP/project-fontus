package com.sap.fontus.taintaware.unified;

public final class IASCompareProxy {
    private IASCompareProxy() {
    }

    @SuppressWarnings("StringEquality")
    public static boolean compareRefEquals(Object a, Object b) {
        if (a != null && b != null) {
            if (a instanceof IASString && b instanceof IASString) {
                return ((IASString) a).getString() == ((IASString) b).getString();
            }
        }
        return a == b;
    }
}
