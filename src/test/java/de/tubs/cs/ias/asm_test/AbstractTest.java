package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

public abstract class AbstractTest {
    private Tainter tainter = new Tainter();

    public static class Tainter {
        public void setTaint(IASTaintAware str, boolean taint) {
            str.setTaint(taint);
        }

        public boolean getTaint(IASTaintAware str) {
            return str.isTainted();
        }
    }

    protected Tainter getTaintChecker() {
        return tainter;
    }
}
