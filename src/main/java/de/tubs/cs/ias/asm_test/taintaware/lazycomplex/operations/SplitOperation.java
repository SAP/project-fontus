package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitOperation implements IASOperation {
    private final String regex;
    /**
     * Index of the splitted string
     */
    private final int index;

    public SplitOperation(String regex, int index) {
        this.regex = regex;
        this.index = index;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(previousString);
        int start = 0;
        for (int counter = 0; matcher.find() && counter < index; counter++) {
            start = matcher.end();
        }
        int end = matcher.start();

        IASTaintRangeUtils.adjustRanges(previousRanges, start, end, start);

        return previousRanges;
    }
}
