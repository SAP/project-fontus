package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitOperation extends IASOperation {
    private final String regex;
    /**
     * Index of the splitted string
     */
    private final int index;

    public SplitOperation(IASLazyComplexAware previous, String regex, int index) {
        super(previous);
        this.regex = regex;
        this.index = index;
    }

    @Override
    public List<IASTaintRange> apply() {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher((CharSequence) previous);
        int start = 0;
        for (int counter = 0; matcher.find() && counter < index; counter++) {
            start = matcher.end();
        }
        int end = matcher.start();

        List<IASTaintRange> taintRanges = this.previous.getTaintRanges();
        IASTaintRangeUtils.adjustRanges(taintRanges, start, end, start);

        return taintRanges;
    }
}
