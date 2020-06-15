package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

public abstract class AbstractTest {
    private Tainter tainter = new Tainter();

    public static class Tainter {
        public void setTaint(IASLazyComplexAware str, boolean taint) {
            str.setTaint(taint);
        }

        public boolean getTaint(IASLazyComplexAware str) {
            return str.isTainted();
        }
    }

    protected Tainter getTaintChecker() {
        return tainter;
    }
}
