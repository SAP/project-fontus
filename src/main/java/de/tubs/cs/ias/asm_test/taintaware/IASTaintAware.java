package de.tubs.cs.ias.asm_test.taintaware;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.io.Serializable;
import java.util.List;

public interface IASTaintAware extends Serializable {
    boolean isTainted();

    void setTaint(boolean taint);

    IASStringable toIASString();

    List<IASTaintRange> getTaintRanges();
}
