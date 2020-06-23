package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASLazyAware;

public abstract class AbstractTest {
    private Tainter tainter = new Tainter();

    public static class Tainter {
        public void setTaint(IASLazyAware str, boolean taint) {
            str.setTaint(taint);
        }

        public boolean getTaint(IASLazyAware str) {
            return str.isTainted();
        }
    }

    protected Tainter getTaintChecker() {
        return tainter;
    }
}
