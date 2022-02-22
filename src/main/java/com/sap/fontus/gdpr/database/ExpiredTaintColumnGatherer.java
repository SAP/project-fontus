package com.sap.fontus.gdpr.database;

import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ExpiredTaintColumnGatherer implements InformationGatherer {
    private String table = "";
    private String catalog = "";
    private int row = 0;
    private final Instant now;

    public ExpiredTaintColumnGatherer() {
        this.now = Instant.now().plus(21, ChronoUnit.DAYS);
    }
    @Override
    public void beginTable(String catalog, String table) {
        this.catalog = catalog;
        this.table = table;
    }

    @Override
    public void endTable() {
        this.catalog = "";
        this.table = "";
        this.row = 0;
    }

    @Override
    public void nextRow() {
        this.row++;
    }

    @Override
    public void taintedColumn(int index, String name, String value, IASTaintInformationable taintInformation) {
        if(Utils.isDataExpired(taintInformation, this.now)) {
            System.out.printf("In %s.%s, row %d, column %s (%d) has an attached, non empty and EXPIRED taint value!%n", this.catalog, this.table, this.row, name, index);
        } else {
            System.out.printf("In %s.%s, row %d, column %s (%d) has an attached, non empty taint value that's not expired!%n", this.catalog, this.table, this.row, name, index);
        }
    }

    @Override
    public void untaintedColumn(int index, String name, Object value) {

    }
}
