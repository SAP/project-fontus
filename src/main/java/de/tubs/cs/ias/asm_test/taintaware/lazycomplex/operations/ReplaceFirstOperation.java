package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRanges;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceFirstOperation extends IASOperation {
    private final IASString regex;
    private final IASString replacement;

    public ReplaceFirstOperation(IASLazyComplexAware previous, IASString regex, IASString replacement) {
        super(previous);
        this.regex = regex;
        this.replacement = replacement;
    }

    @Override
    public List<IASTaintRange> apply() {
        List<IASTaintRange> taintRanges = this.previous.getTaintRanges();
        IASTaintRanges ranges = new IASTaintRanges(taintRanges);

        // Is one of both Strings tainted? If not, it's irrelevant if one happened for the tainting
        if (this.previous.isTainted() || this.replacement.isTainted()) {
            Matcher matcher = Pattern.compile(this.regex.toString()).matcher(this.previous.toString());
            if (matcher.find()) {
                final int start = matcher.start();
                final int end = matcher.end();

                ranges.replaceTaintInformation(start, end, replacement.getTaintRanges(), replacement.length(), true);
            }
        }
        return ranges.getAllRanges();
    }
}
