package de.tubs.cs.ias.asm_test.utils;

public enum Statistics {
    INSTANCE;

    private static final int PRINT_INTERVAL = 10;

    private long stringCount;
    private long taintRangeSum;
    private long untaintedStringCount;

    Statistics() {
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
                        "Average number of taint ranges per String: %f\n",
                stringCount,
                untaintedStringCount,
                stringCount - untaintedStringCount,
                getZeroTaintRangeShare(),
                getRangeCountAverage()
        ));
    }
}
