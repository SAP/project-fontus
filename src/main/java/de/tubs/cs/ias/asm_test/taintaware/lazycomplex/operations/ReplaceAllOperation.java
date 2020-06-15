package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASStringBuilder;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASMatcherReplacement;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceAllOperation implements IASOperation {
    private final IASString regex;
    private final IASString replacement;

    public ReplaceAllOperation(IASString regex, IASString replacement) {
        this.regex = regex;
        this.replacement = replacement;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        Matcher matcher = Pattern.compile(this.regex.toString()).matcher(previousString);
        int appendPos = 0;
        int length = 0;
        int previousEnd = 0;
        List<IASTaintRange> ranges = new ArrayList<>();
        while (matcher.find()) {
            IASMatcherReplacement replacer = IASMatcherReplacement.createReplacement(replacement, new IASStringBuilder());
            int end = matcher.start();

            List<IASTaintRange> currRanges = new ArrayList<>(previousRanges);
            IASTaintRangeUtils.adjustAndRemoveRanges(currRanges, appendPos, end, appendPos - length);
            ranges.addAll(currRanges);

            length += end - previousEnd;
            previousEnd = matcher.end();

            IASString repl = (IASString) replacer.doReplacement(matcher, new IASString(previousString, previousRanges), new IASStringBuilder());
            int currReplLength = repl.length();

            List<IASTaintRange> replRanges = repl.getTaintRanges();
            IASTaintRangeUtils.shiftRight(replRanges, length);
            ranges.addAll(replRanges);

            length += currReplLength;
            appendPos = matcher.end();
        }
        List<IASTaintRange> currRanges = new ArrayList<>(previousRanges);
        IASTaintRangeUtils.adjustAndRemoveRanges(currRanges, appendPos, previousString.length(), appendPos - length);
        ranges.addAll(currRanges);
        return ranges;
    }
}
