package de.tubs.cs.ias.asm_test.utils.stats;

import de.tubs.cs.ias.asm_test.Constants;

import javax.management.*;
import java.lang.management.ManagementFactory;

public enum Statistics implements StatisticsMXBean {
    INSTANCE;

    private static final int PRINT_INTERVAL = 10;

    private long stringCount;
    private long taintRangeSum;
    private long untaintedStringCount;
    private long lazyTaintInformationCreated;
    private long lazyTaintInformationEvaluated;
    private long initializedStrings;
    private long taintCheckUntainted;
    private long taintCheckTainted;

    Statistics() {
        register();
    }

    @Override
    public synchronized void reset() {
        stringCount = 0;
        taintRangeSum = 0;
        untaintedStringCount = 0;
        lazyTaintInformationCreated = 0;
        lazyTaintInformationEvaluated = 0;
        taintCheckUntainted = 0;
        taintCheckTainted = 0;
        initializedStrings = 0;
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

    @Override
    public synchronized double getRangeCountAverage() {
        return ((double) taintRangeSum) / stringCount;
    }

    @Override
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


    private void register() {
        try {
            ObjectName name = new ObjectName(Constants.PACKAGE, "type", Statistics.class.getSimpleName());
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, name);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getStringCount() {
        return stringCount;
    }

    @Override
    public long getTaintRangeSum() {
        return taintRangeSum;
    }

    @Override
    public long getUntaintedStringCount() {
        return untaintedStringCount;
    }

    @Override
    public long getLazyCreatedCount() {
        return getLazyTaintInformationCreated();
    }

    @Override
    public long getLazyEvaluatedCount() {
        return getLazyTaintInformationEvaluated();
    }

    public long getLazyTaintInformationCreated() {
        return lazyTaintInformationCreated;
    }

    public long getLazyTaintInformationEvaluated() {
        return lazyTaintInformationEvaluated;
    }

    public synchronized void incrementInitialized() {
        this.initializedStrings++;
    }

    @Override
    public synchronized long getInitializedStrings() {
        return initializedStrings;
    }

    public synchronized void recordTaintCheck(boolean isTainted) {
        if(isTainted) {
            taintCheckTainted++;
        } else {
            taintCheckUntainted++;
        }
    }

    @Override
    public long getTaintChecked() {
        return taintCheckTainted + taintCheckUntainted;
    }

    @Override
    public long getTaintCheckUntainted() {
        return taintCheckUntainted;
    }

    @Override
    public long getTaintCheckTainted() {
        return taintCheckTainted;
    }
}
