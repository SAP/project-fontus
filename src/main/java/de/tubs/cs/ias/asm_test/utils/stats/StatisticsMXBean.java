package de.tubs.cs.ias.asm_test.utils.stats;

public interface StatisticsMXBean {
    void reset();

    long getStringCount();

    long getTaintRangeSum();

    long getUntaintedStringCount();

    long getLazyCreatedCount();

    long getLazyEvaluatedCount();

    double getZeroTaintRangeShare();

    double getRangeCountAverage();
}
