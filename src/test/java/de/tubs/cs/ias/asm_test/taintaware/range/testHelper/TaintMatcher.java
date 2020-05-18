package de.tubs.cs.ias.asm_test.taintaware.range.testHelper;

import de.tubs.cs.ias.asm_test.taintaware.range.IASTaintInformation;
import de.tubs.cs.ias.asm_test.taintaware.range.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.range.IASRangeAware;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;
@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class TaintMatcher {
    public static Matcher<Object> taintEquals(List<IASTaintRange> ranges) {
        return new BaseMatcher<Object>() {
            boolean taintNotInitialized;
            Object operand;

            @Override
            public void describeTo(Description description) {
                description.appendValue(ranges);
            }

            @Override
            public void describeMismatch(Object s, Description mismatchDescription) {
                if (taintNotInitialized) {
                    mismatchDescription.appendText("String '").appendValue(s).appendText("' is not taint-aware!");
                    return;
                }

                mismatchDescription.appendText("was ").appendValue(((IASRangeAware) s).getTaintInformation().getAllRanges());
            }

            @Override
            public boolean matches(Object s) {
                assert s instanceof IASRangeAware;

                operand = s;
                taintNotInitialized = THelper.isUninitialized((IASRangeAware) s);

                if (taintNotInitialized) {
                    return false;
                }

                IASTaintInformation tI = THelper.get((IASRangeAware) s);

                return tI.getAllRanges().equals(ranges);
            }
        };
    }

    public static Matcher<Object> taintEquals(RangeChainer rangeChainer) {
        return taintEquals(rangeChainer.done());
    }

    public static Matcher<Object> taintUninitialized() {
        return new BaseMatcher<Object>() {
            Object operand;

            @Override
            public boolean matches(Object item) {
                operand = item;

                return THelper.isUninitialized((IASRangeAware) item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("\"" + operand.toString() + "\"'s taint property should be uninitialized.");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("is initialized already.");
            }
        };
    }
}
