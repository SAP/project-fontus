package com.sap.fontus.utils.stats;

import com.sap.fontus.Constants;
import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.taintaware.range.IASTaintInformation;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

// Todo: Convert to atomics
public enum Statistics implements StatisticsMXBean {
    INSTANCE;

    private long stringCount = 0L;
    private long taintRangeSum = 0L;
    private long untaintedStringCount = 0L;
    private long lazyTaintInformationCreated = 0L;
    private long lazyTaintInformationEvaluated = 0L;
    private long initializedStrings = 0L;
    private long taintCheckUntainted = 0L;
    private long taintCheckTainted = 0L;
    private long lazyThresholdExceededCount = 0L;
    private final Map<String, Long> taintlossHits = new HashMap<>();

    Statistics() {
        this.register();
    }

    @Override
    public synchronized void reset() {
        this.stringCount = 0L;
        this.taintRangeSum = 0L;
        this.untaintedStringCount = 0L;
        this.lazyTaintInformationCreated = 0L;
        this.lazyTaintInformationEvaluated = 0L;
        this.taintCheckUntainted = 0L;
        this.taintCheckTainted = 0L;
        this.initializedStrings = 0L;
        this.taintlossHits.clear();
    }

    public synchronized void incrementTaintlossHits(String call) {
        this.taintlossHits.compute(call, ((tuple, longVal) -> {
            if (longVal == null) {
                return 1L;
            } else {
                return longVal + 1L;
            }
        }));
    }

    public synchronized void incrementLazyTaintInformationCreated() {
        this.lazyTaintInformationCreated++;
    }

    public synchronized void incrementLazyTaintInformationEvaluated() {
        this.lazyTaintInformationEvaluated++;
    }

    public synchronized void incrementLazyThresholdExceededCount() {
        this.lazyThresholdExceededCount++;
    }

    public synchronized void addRangeCount(IASTaintInformationable taintInformationable) {
        this.stringCount++;
        int rangeCount = 0;
        if (taintInformationable instanceof IASTaintInformation) {
            rangeCount = ((IASTaintInformation) taintInformationable).getTaintRanges().getTaintRanges().size();
        }

        this.taintRangeSum += rangeCount;
        if (rangeCount == 0) {
            this.untaintedStringCount++;
        }
        long tainted = this.stringCount - this.untaintedStringCount;
    }

    @Override
    public synchronized double getRangeCountAverage() {
        return ((double) this.taintRangeSum) / this.stringCount;
    }

    @Override
    public synchronized double getZeroTaintRangeShare() {
        return ((double) this.untaintedStringCount) / this.stringCount;
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
        return this.stringCount;
    }

    @Override
    public long getTaintRangeSum() {
        return this.taintRangeSum;
    }

    @Override
    public long getUntaintedStringCount() {
        return this.untaintedStringCount;
    }

    @Override
    public long getLazyCreatedCount() {
        return this.lazyTaintInformationCreated;
    }

    @Override
    public long getLazyEvaluatedCount() {
        return this.lazyTaintInformationEvaluated;
    }

    @Override
    public long getLazyThresholdExceededCount() {
        return this.lazyThresholdExceededCount;
    }

    public long getLazyTaintInformationCreated() {
        return this.lazyTaintInformationCreated;
    }

    public long getLazyTaintInformationEvaluated() {
        return this.lazyTaintInformationEvaluated;
    }

    public synchronized void incrementInitialized() {
        this.initializedStrings++;
    }

    @Override
    public synchronized long getInitializedStrings() {
        return this.initializedStrings;
    }

    public synchronized void recordTaintCheck(boolean isTainted) {
        if (isTainted) {
            this.taintCheckTainted++;
        } else {
            this.taintCheckUntainted++;
        }
    }

    @Override
    public long getTaintChecked() {
        return this.taintCheckTainted + this.taintCheckUntainted;
    }

    @Override
    public long getTaintCheckUntainted() {
        return this.taintCheckUntainted;
    }

    @Override
    public long getTaintCheckTainted() {
        return this.taintCheckTainted;
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
