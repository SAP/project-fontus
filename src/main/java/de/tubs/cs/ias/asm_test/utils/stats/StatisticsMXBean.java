package de.tubs.cs.ias.asm_test.utils.stats;

public interface StatisticsMXBean {
    long getStringCount();

    long getTaintRangeSum();

    long getUntaintedStringCount();

    long getLazyCreatedCount();

    long getLazyEvaluatedCount();

    double getZeroTaintRangeShare();

    double getRangeCountAverage();
}
