package de.tubs.cs.ias.asm_test.taintaware;

import java.io.Serializable;

public interface IASTaintAware extends Serializable {
    boolean isTainted();

    void setTaint(boolean taint);
}
