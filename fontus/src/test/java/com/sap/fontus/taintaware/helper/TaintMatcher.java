package com.sap.fontus.taintaware.helper;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.range.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;

public final class TaintMatcher {
    private TaintMatcher() {
    }

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
                if (this.taintNotInitialized) {
                    mismatchDescription.appendText("String '").appendValue(s).appendText("' is not taint-aware!");
                    return;
                }

                mismatchDescription.appendText("was ").appendValue(((IASTaintInformation) ((IASTaintAware) s).getTaintInformation()).getTaintRanges().getTaintRanges());
            }

            @Override
            public boolean matches(Object s) {
                assert s instanceof IASTaintAware;

                this.operand = s;
                this.taintNotInitialized = THelper.isUninitialized((IASTaintAware) s);

                if (this.taintNotInitialized) {
                    return false;
                }

                IASTaintInformation tI = (IASTaintInformation) THelper.get((IASTaintAware) s);

                return tI.getTaintRanges().getTaintRanges().equals(ranges);
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
                this.operand = item;

                return THelper.isUninitialized((IASTaintAware) item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("\"" + this.operand.toString() + "\"'s taint property should be uninitialized.");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("is initialized already.");
            }
        };
    }
}
