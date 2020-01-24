package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

public interface IASRangeAware extends IASTaintAware {
    IASTaintInformation getTaintInformation();
    boolean isUninitialized();
    void initialize();
}
