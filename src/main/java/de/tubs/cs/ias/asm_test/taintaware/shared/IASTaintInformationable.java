package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.io.Serializable;
import java.util.List;

public interface IASTaintInformationable extends Serializable {
    /**
     * Calculates the current taint ranges.
     *
     * @return Returns a new list that can be modified without affecting this IASTaintInformation
     */
    List<IASTaintRange> getTaintRanges();

    boolean isTainted();

    IASTaintSource getTaintFor(int position);

    boolean isTaintedAt(int index);
}
