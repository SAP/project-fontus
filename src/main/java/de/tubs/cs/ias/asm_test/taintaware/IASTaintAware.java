package de.tubs.cs.ias.asm_test.taintaware;

public interface IASTaintAware {
    boolean isTainted();

    void setTaint(boolean taint);
}
