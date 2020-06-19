package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRanges;

import java.util.List;

public class IASTaintInformation extends IASTaintRanges {
    public IASTaintInformation() {
    }

    public IASTaintInformation(List<IASTaintRange> ranges) {
        super(ranges);
    }
}
