package de.tubs.cs.ias.asm_test;

public interface IASTaintAware {
    boolean isTainted();
    void setTaint(boolean taint);
}
