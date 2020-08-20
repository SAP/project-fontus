package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.io.Serializable;
import java.util.List;

public interface IASOperation extends Serializable {
    List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint);
}
