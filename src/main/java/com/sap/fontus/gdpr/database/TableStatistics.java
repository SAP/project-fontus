package com.sap.fontus.gdpr.database;

import java.util.ArrayList;
import java.util.Collection;

public class TableStatistics {

    private final String catalog;
    private final String name;
    private final Collection<RowStatistics> rowStats = new ArrayList<>();
    private RowStatistics currentRow = null;

    public TableStatistics(String catalog, String name) {
        this.catalog = catalog;
        this.name = name;
    }

    void nextRow() {
        if (this.currentRow != null) {
            this.rowStats.add(this.currentRow);
        }
        this.currentRow = new RowStatistics();
    }

    public void endTable() {
        if (this.currentRow != null) {
            this.rowStats.add(this.currentRow);
        }
        this.currentRow = null;
    }

    public void incrementTainted() {
        this.currentRow.incrementTainted();
    }

    public void incrementUntainted() {
        this.currentRow.incrementUntainted();
    }

    public void printTableStatistics() {
        int total = this.rowStats.size();
        int hasTainted = 0;
        int oneThird = 0;
        int half = 0;
        int twoThirds = 0;
        for (RowStatistics row : this.rowStats) {
            if (row.getTainted() > 0) {
                hasTainted++;
                double perc = row.taintedPercentage();
                if (perc > 66.6) {
                    twoThirds++;
                }
                if (perc > 50.0) {
                    half++;
                }
                if (perc > 33.3) {
                    oneThird++;
                }
            }
        }

        if (hasTainted == 0) {
            System.out.printf("%s.%s has no rows with tainted values!%n", this.catalog, this.name);
        } else {
            System.out.printf("%s.%s has %d rows with tainted values!%n", this.catalog, this.name, hasTainted);
            System.out.printf("\tRows with > 33.3%% of tainted values: %d%n", oneThird);
            System.out.printf("\tRows with > 50%% of tainted values: %d%n", half);
            System.out.printf("\tRows with > 66.6%% of tainted values: %d%n", twoThirds);
        }
    }

}
