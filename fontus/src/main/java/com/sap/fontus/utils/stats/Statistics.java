package com.sap.fontus.utils.stats;

import com.sap.fontus.Constants;
import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.taintaware.range.IASTaintInformation;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public enum Statistics implements StatisticsMXBean {
    INSTANCE;

    private final AtomicLong stringCount;
    private final AtomicLong taintRangeSum ;
    private final AtomicLong untaintedStringCount;
    private final AtomicLong lazyTaintInformationCreated;
    private final AtomicLong lazyTaintInformationEvaluated;
    private final AtomicLong initializedStrings;
    private final AtomicLong taintCheckUntainted;
    private final AtomicLong taintCheckTainted;
    private final AtomicLong lazyThresholdExceededCount;

    private final AtomicLong rewrittenQueries;
    private final AtomicLong totalQueries;
    private final AtomicLong rewrittenQueryLength;
    private final AtomicLong totalQueriesLength;
    private final Map<String, Long> taintlossHits = new ConcurrentHashMap<>();

    Statistics() {
        this.stringCount = new AtomicLong();
        this.taintRangeSum = new AtomicLong();
        this.untaintedStringCount = new AtomicLong();
        this.lazyTaintInformationCreated = new AtomicLong();
        this.lazyTaintInformationEvaluated = new AtomicLong();
        this.initializedStrings = new AtomicLong();
        this.taintCheckUntainted = new AtomicLong();
        this.taintCheckTainted = new AtomicLong();
        this.lazyThresholdExceededCount = new AtomicLong();
        this.rewrittenQueries = new AtomicLong();
        this.totalQueries = new AtomicLong();
        this.rewrittenQueryLength = new AtomicLong();
        this.totalQueriesLength = new AtomicLong();
        this.register();
    }

    @Override
    public void reset() {
        this.stringCount.set(0L);
        this.taintRangeSum.set(0L);
        this.untaintedStringCount.set(0L);
        this.lazyTaintInformationCreated.set(0L);
        this.lazyTaintInformationEvaluated.set(0L);
        this.taintCheckUntainted.set(0L);
        this.taintCheckTainted.set(0L);
        this.initializedStrings.set(0L);
        this.rewrittenQueries.set(0L);
        this.totalQueries.set(0L);
        this.totalQueriesLength.set(0L);
        this.rewrittenQueryLength.set(0L);
        this.taintlossHits.clear();
    }

    public void incrementTaintlossHits(String call) {
        this.taintlossHits.merge(call, 1L, Long::sum);
    }

    public  void incrementLazyTaintInformationCreated() {
        this.lazyTaintInformationCreated.incrementAndGet();
    }

    public  void incrementLazyTaintInformationEvaluated() {
        this.lazyTaintInformationEvaluated.incrementAndGet();
    }

    public  void incrementLazyThresholdExceededCount() {
        this.lazyThresholdExceededCount.incrementAndGet();
    }

    public  void incrementRewrittenQueries() {
        this.rewrittenQueries.incrementAndGet();
    }

    public void incrementTotalQueries() {
        this.totalQueries.incrementAndGet();
    }

    public  void incrementRewrittenQueryLength(long x) {
        this.rewrittenQueryLength.addAndGet(x);
    }

    public void incrementTotalQueryLength(long x) {
        this.totalQueriesLength.addAndGet(x);
    }

    public  void addRangeCount(IASTaintInformationable taintInformationable) {
        long rangeCount = 0L;
        if (taintInformationable instanceof IASTaintInformation) {
            rangeCount = ((IASTaintInformation) taintInformationable).getTaintRanges().getTaintRanges().size();
        }

        this.taintRangeSum.addAndGet(rangeCount);
        long untaintedCount = this.untaintedStringCount.get();
        if (rangeCount == 0L) {
            untaintedCount = this.untaintedStringCount.incrementAndGet();
        }
        long tainted = this.stringCount.incrementAndGet() - untaintedCount;
    }

    @Override
    public double getRangeCountAverage() {
        return ((double) this.taintRangeSum.get()) / this.stringCount.get();
    }

    @Override
    public  double getZeroTaintRangeShare() {
        return ((double) this.untaintedStringCount.get()) / this.stringCount.get();
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
        return this.stringCount.get();
    }

    @Override
    public long getTaintRangeSum() {
        return this.taintRangeSum.get();
    }

    @Override
    public long getUntaintedStringCount() {
        return this.untaintedStringCount.get();
    }

    @Override
    public long getLazyCreatedCount() {
        return this.lazyTaintInformationCreated.get();
    }

    @Override
    public long getLazyEvaluatedCount() {
        return this.lazyTaintInformationEvaluated.get();
    }

    @Override
    public long getLazyThresholdExceededCount() {
        return this.lazyThresholdExceededCount.get();
    }

    public long getLazyTaintInformationCreated() {
        return this.lazyTaintInformationCreated.get();
    }

    public long getLazyTaintInformationEvaluated() {
        return this.lazyTaintInformationEvaluated.get();
    }

    public void incrementInitialized() {
        this.initializedStrings.incrementAndGet();
    }

    @Override
    public long getInitializedStrings() {
        return this.initializedStrings.get();
    }

    public void recordTaintCheck(boolean isTainted) {
        if (isTainted) {
            this.taintCheckTainted.incrementAndGet();
        } else {
            this.taintCheckUntainted.incrementAndGet();
        }
    }

    @Override
    public long getTaintChecked() {
        return this.taintCheckTainted.get() + this.taintCheckUntainted.get();
    }

    @Override
    public long getTaintCheckUntainted() {
        return this.taintCheckUntainted.get();
    }

    @Override
    public long getTaintCheckTainted() {
        return this.taintCheckTainted.get();
    }

    @Override
    public long getRewrittenSQLQueries() {
        return this.rewrittenQueries.get();
    }

    @Override
    public long getTotalSQLQueries() {
        return this.totalQueries.get();
    }

    @Override
    public long getTotalRewrittenSQLQueryLength() {
        return this.rewrittenQueryLength.get();
    }

    @Override
    public long getTotalSQLQueryLength() {
        return this.totalQueriesLength.get();
    }

    @Override
    public double getAverageRewrittenSQLQueryLength() {
        return this.rewrittenQueries.get() > 0 ? (double) this.rewrittenQueryLength.get() / (double) this.rewrittenQueries.get() : 0;
    }

    @Override
    public double getAverageSQLQueryLength() {
        return this.rewrittenQueries.get() > 0 ? (double) this.totalQueriesLength.get() / (double) this.rewrittenQueries.get() : 0;
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
