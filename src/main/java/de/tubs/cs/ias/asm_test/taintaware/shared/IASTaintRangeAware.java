package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.util.List;

public interface IASTaintRangeAware extends IASTaintAware {
    List<IASTaintRange> getTaintRanges();
    boolean isUninitialized();
    void initialize();
    boolean isTaintedAt(int index);
    void setTaint(IASTaintSource source);
    void setTaint(List<IASTaintRange> ranges);
}
