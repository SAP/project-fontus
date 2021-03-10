package com.sap.fontus.taintaware.range.testHelper;

import com.sap.fontus.taintaware.range.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRangeAware;
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

                mismatchDescription.appendText("was ").appendValue(((IASTaintInformation) ((IASTaintRangeAware) s).getTaintInformation()).getTaintRanges());
            }

            @Override
            public boolean matches(Object s) {
                assert s instanceof IASTaintRangeAware;

                operand = s;
                taintNotInitialized = THelper.isUninitialized((IASTaintRangeAware) s);

                if (taintNotInitialized) {
                    return false;
                }

                IASTaintInformation tI = (IASTaintInformation) THelper.get((IASTaintRangeAware) s);

                return tI.getTaintRanges().equals(ranges);
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

                return THelper.isUninitialized((IASTaintRangeAware) item);
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
