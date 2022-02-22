package com.sap.fontus.gdpr.database;

import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

class ExpiredTaintColumnGatherer extends AbstractInformationGatherer {
    private final Instant now;

    ExpiredTaintColumnGatherer() {
        super();
        this.now = Instant.now().plus(21, ChronoUnit.DAYS);
    }

    @Override
    public void taintedColumn(int index, String name, String value, IASTaintInformationable taintInformation) {
        if (Utils.isDataExpired(taintInformation, this.now)) {
            System.out.printf("In %s.%s, row %d, column %s (%d) has an attached, non empty and EXPIRED taint value!%n", this.catalog, this.table, this.row, name, index);
        } else {
            System.out.printf("In %s.%s, row %d, column %s (%d) has an attached, non empty taint value that's not expired!%n", this.catalog, this.table, this.row, name, index);
        }
    }
}
