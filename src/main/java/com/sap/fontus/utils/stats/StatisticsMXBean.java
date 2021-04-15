package com.sap.fontus.utils.stats;

import java.util.Map;

public interface StatisticsMXBean {
    void reset();

    long getStringCount();

    long getTaintRangeSum();

    long getUntaintedStringCount();

    long getLazyCreatedCount();

    long getLazyEvaluatedCount();

    long getLazyThresholdExceededCount();

    double getZeroTaintRangeShare();

    double getRangeCountAverage();

    long getInitializedStrings();

    long getTaintChecked();

    long getTaintCheckUntainted();

    long getTaintCheckTainted();

    Map<String, Long> getTaintlossHits();
}
