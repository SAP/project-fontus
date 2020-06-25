package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeAware;

public interface IASArrayAware extends IASTaintRangeAware {
    int[] getTaints();
}
