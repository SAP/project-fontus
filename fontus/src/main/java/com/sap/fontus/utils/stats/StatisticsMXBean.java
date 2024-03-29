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

    long getRewrittenSQLQueries();

    long getTotalSQLQueries();

    long getTotalSQLQueryLength();

    long getTotalRewrittenSQLQueryLength();

    double getAverageRewrittenSQLQueryLength();

    double getAverageSQLQueryLength();

    Map<String, Long> getTaintlossHits();

    void saveClassBytecode(String qn);

    double getSourceCoverage();

    double getSinkCoverage();

    double getTaintedSinkCoverage();

    double getUniqueSourceCoverage();

    double getUniqueSinkCoverage();

    long getSourceCount();
    long getUniqueSourceCount();

    long getSinkCount();
    long getUniqueSinkCount();

}
