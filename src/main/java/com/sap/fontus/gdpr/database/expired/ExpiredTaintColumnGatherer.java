package com.sap.fontus.gdpr.database.expired;

import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.database.AbstractInformationGatherer;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import picocli.CommandLine;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ExpiredTaintColumnGatherer extends AbstractInformationGatherer {
    private final Instant now;

    @CommandLine.Option(
            names = {"-d", "--days"},
            required = true,
            paramLabel = "dates until expiry",
            description = "Number of days in the future the data expires"
    )
    private int days = 21;

    public ExpiredTaintColumnGatherer() {
        super();
        this.now = Instant.now().plus(this.days, ChronoUnit.DAYS);
    }

    @Override
    public void taintedColumn(int index, String name, String type, String value, IASTaintInformationable taintInformation) {
        if (Utils.isDataExpired(taintInformation, this.now)) {
            System.out.printf("In %s.%s, row %d, column %s (%d) has an attached, non empty and EXPIRED taint value!%n", this.catalog, this.table, this.row, name, index);
        } else {
            //System.out.printf("In %s.%s, row %d, column %s (%d) has an attached, non empty taint value that's not expired!%n", this.catalog, this.table, this.row, name, index);
        }
    }
}
