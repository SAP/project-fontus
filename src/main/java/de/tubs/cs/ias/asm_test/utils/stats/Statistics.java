package de.tubs.cs.ias.asm_test.utils.stats;

public enum Statistics {
    INSTANCE;

    private static final int PRINT_INTERVAL = 10;

    private long stringCount;
    private long taintRangeSum;
    private long untaintedStringCount;
    private long lazyTaintInformationCreated;
    private long lazyTaintInformationEvaluated;

    Statistics() {
        StatisticsMXBeanImpl.register();
    }

    public synchronized void incrementLazyTaintInformationCreated() {
        lazyTaintInformationCreated++;
    }

    public synchronized void incrementLazyTaintInformationEvaluated() {
        lazyTaintInformationEvaluated++;
    }

    public synchronized void addRangeCount(int rangeCount) {
        stringCount++;
        taintRangeSum += rangeCount;
        if (rangeCount == 0) {
            untaintedStringCount++;
        }
        long tainted = stringCount - untaintedStringCount;
        if (tainted > 0 && tainted % PRINT_INTERVAL == 0) {
            printStatistics();
        }
    }

    public synchronized double getRangeCountAverage() {
        return ((double) taintRangeSum) / stringCount;
    }

    public synchronized double getZeroTaintRangeShare() {
        return ((double) untaintedStringCount) / stringCount;
    }

    public synchronized void printStatistics() {
        System.out.println(String.format(
                "Total IASString count: %d\n" +
                        "Untainted IASString count %d\n" +
                        "Tainted IASString count %d\n" +
                        "Share of IASStrings with zero taint ranges: %f\n" +
                        "Average number of taint ranges per String: %f\n" +
                        "Number of lazy TI's created: %d\n" +
                        "Number of lazy TI's evaluated: %d\n",
                stringCount,
                untaintedStringCount,
                stringCount - untaintedStringCount,
                getZeroTaintRangeShare(),
                getRangeCountAverage(),
                lazyTaintInformationCreated,
                lazyTaintInformationEvaluated
        ));
    }

    public long getStringCount() {
        return stringCount;
    }

    public long getTaintRangeSum() {
        return taintRangeSum;
    }

    public long getUntaintedStringCount() {
        return untaintedStringCount;
    }

    public long getLazyTaintInformationCreated() {
        return lazyTaintInformationCreated;
    }

    public long getLazyTaintInformationEvaluated() {
        return lazyTaintInformationEvaluated;
    }
}
