package de.tubs.cs.ias.asm_test.taintaware.shared;

public class IASCompareProxy {
    @SuppressWarnings("StringEquality")
    public static boolean compareRefEquals(Object a, Object b) {
        if (a != null && b != null) {
            if (a instanceof IASStringable && b instanceof IASStringable) {
                return ((IASStringable) a).getString() == ((IASStringable) b).getString();
            }
        }
        return a == b;
    }
}
