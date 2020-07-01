package de.tubs.cs.ias.asm_test;

import java.util.ArrayList;
import java.util.List;

public enum Statistics {
    INSTANCE;

    private final List<Integer> rangeCount;

    Statistics() {
        rangeCount = new ArrayList<>();
    }

    public void addRangeCount(int rangeCount) {
        this.rangeCount.add(rangeCount);
        if (this.rangeCount.size() % 100 == 0) {
            printStatistics();
        }
    }

    public double getRangeCountAverage() {
        return rangeCount.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    public double getZeroTaintRangeShare() {
        return ((double) rangeCount.stream().filter(integer -> integer == 0).count()) / rangeCount.size();
    }

    public double getRangeCountMedian() {
        rangeCount.sort(Integer::compareTo);
        if (rangeCount.size() == 0) {
            return 0;
        }
        return rangeCount.get(rangeCount.size() / 2);
    }

    public void printStatistics() {
        System.out.println(String.format(
                "Total IASString count: %d\n" +
                        "Share of IASStrings with zero taint ranges: %f\n" +
                        "Average number of taint ranges per String: %f\n" +
                        "Median number of taint ranges per String: %f",
                rangeCount.size(),
                getZeroTaintRangeShare(),
                getRangeCountAverage(),
                getRangeCountMedian()));
    }
}
