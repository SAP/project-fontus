package de.tubs.cs.ias.asm_test.taintaware;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.io.Serializable;
import java.util.List;

public interface IASTaintAware extends Serializable {
    boolean isTainted();

    void setTaint(boolean taint);

    void setTaint(IASTaintSource source);

    IASStringable toIASString();

    List<IASTaintRange> getTaintRanges();
}
