package com.sap.fontus.taintaware.unified;

public final class IASCompareProxy {
    private IASCompareProxy() {
    }

    @SuppressWarnings("StringEquality")
    public static boolean compareRefEquals(Object a, Object b) {
        if (a != null && b != null) {
            if (a instanceof IASString sa && b instanceof IASString sb) {
                return sa.getString() == sb.getString();
            }
        }
        return a == b;
    }
}
