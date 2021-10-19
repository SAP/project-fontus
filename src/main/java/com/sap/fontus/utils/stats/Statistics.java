package com.sap.fontus.utils.stats;

import com.sap.fontus.Constants;
import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.taintaware.range.IASTaintInformation;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public enum Statistics implements StatisticsMXBean {
    INSTANCE;

    private long stringCount;
    private long taintRangeSum;
    private long untaintedStringCount;
    private long lazyTaintInformationCreated;
    private long lazyTaintInformationEvaluated;
    private long initializedStrings;
    private long taintCheckUntainted;
    private long taintCheckTainted;
    private long lazyThresholdExceededCount;
    private Map<String, Long> taintlossHits = new HashMap<>();

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
        this.taintlossHits.clear();
    }

    public synchronized void incrementTaintlossHits(String call) {
        this.taintlossHits.compute(call, ((tuple, longVal) -> {
            if (longVal == null) {
                return 1L;
            } else {
                return longVal + 1;
            }
        }));
    }

    public synchronized void incrementLazyTaintInformationCreated() {
        lazyTaintInformationCreated++;
    }

    public synchronized void incrementLazyTaintInformationEvaluated() {
        lazyTaintInformationEvaluated++;
    }

    public synchronized void incrementLazyThresholdExceededCount() {
        lazyThresholdExceededCount++;
    }

    public synchronized void addRangeCount(IASTaintInformationable taintInformationable) {
        stringCount++;
        int rangeCount = 0;
        if (taintInformationable instanceof IASTaintInformation) {
            rangeCount = ((IASTaintInformation) taintInformationable).getTaintRanges().getTaintRanges().size();
        }

        taintRangeSum += rangeCount;
        if (rangeCount == 0) {
            untaintedStringCount++;
        }
        long tainted = stringCount - untaintedStringCount;
    }

    @Override
    public synchronized double getRangeCountAverage() {
        return ((double) taintRangeSum) / stringCount;
    }

    @Override
    public synchronized double getZeroTaintRangeShare() {
        return ((double) untaintedStringCount) / stringCount;
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

    @Override
    public long getLazyThresholdExceededCount() {
        return this.lazyThresholdExceededCount;
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
        if (isTainted) {
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

    @Override
    public Map<String, Long> getTaintlossHits() {
        return this.taintlossHits;
    }

    @Override
    public void saveClassBytecode(String qn) {
        TaintAgent.logInstrumentedClass(qn);
    }
}
