package de.tubs.cs.ias.asm_test.utils.stats;

import de.tubs.cs.ias.asm_test.Constants;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

public class StatisticsMXBeanImpl implements StatisticsMXBean {
    @Override
    public long getStringCount() {
        return Statistics.INSTANCE.getStringCount();
    }

    @Override
    public long getTaintRangeSum() {
        return Statistics.INSTANCE.getTaintRangeSum();
    }

    @Override
    public long getUntaintedStringCount() {
        return Statistics.INSTANCE.getUntaintedStringCount();
    }

    @Override
    public long getLazyCreatedCount() {
        return Statistics.INSTANCE.getLazyTaintInformationCreated();
    }

    @Override
    public long getLazyEvaluatedCount() {
        return Statistics.INSTANCE.getLazyTaintInformationEvaluated();
    }

    @Override
    public double getZeroTaintRangeShare() {
        return Statistics.INSTANCE.getZeroTaintRangeShare();
    }

    @Override
    public double getRangeCountAverage() {
        return Statistics.INSTANCE.getRangeCountAverage();
    }

    public static void register() {
        try {
            ObjectName name = new ObjectName(Constants.PACKAGE, "type", StatisticsMXBeanImpl.class.getSimpleName());
            ManagementFactory.getPlatformMBeanServer().registerMBean(new StatisticsMXBeanImpl(), name);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            e.printStackTrace();
        }
    }
}
