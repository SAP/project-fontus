package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString;
import de.tubs.cs.ias.asm_test.taintaware.range.IASStringBuilder;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASMatcherReplacement;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASStringable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceAllOperation extends IASOperation {
    private final IASString regex;
    private final IASString replacement;

    public ReplaceAllOperation(IASLazyComplexAware previous, IASString regex, IASString replacement) {
        super(previous);
        this.regex = regex;
        this.replacement = replacement;
    }

    @Override
    public List<IASTaintRange> apply() {
        Matcher matcher = Pattern.compile(this.regex.toString()).matcher(this.previous.toString());
        int appendPos = 0;
        int length = 0;
        int previousEnd = 0;
        List<IASTaintRange> ranges = new ArrayList<>();
        while (matcher.find()) {
            IASMatcherReplacement replacer = IASMatcherReplacement.createReplacement(replacement, new IASStringBuilder());
            int end = matcher.start();

            List<IASTaintRange> currRanges = this.previous.getTaintRanges();
            IASTaintRangeUtils.adjustRanges(currRanges, appendPos, end, appendPos - length);
            ranges.addAll(currRanges);

            length += end - previousEnd;
            previousEnd = matcher.end();

            IASString repl = (IASString) replacer.doReplacement(matcher, (IASStringable) this.previous, new IASStringBuilder());
            int currReplLength = repl.length();

            List<IASTaintRange> replRanges = repl.getTaintRanges();
            IASTaintRangeUtils.shiftRight(replRanges, length);
            ranges.addAll(replRanges);

            length += currReplLength;
            appendPos = matcher.end();
        }
        List<IASTaintRange> currRanges = this.previous.getTaintRanges();
        IASTaintRangeUtils.adjustRanges(currRanges, appendPos, this.previous.length(), appendPos - length);
        ranges.addAll(currRanges);
        return ranges;
    }
}
