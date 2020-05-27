package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

public interface IASArrayAware extends IASTaintAware {
    int[] getTaints();
}
