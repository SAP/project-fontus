package com.sap.fontus.gdpr.database.statistics;

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

    public void incrementStringColumn() {
        this.currentRow.incrementStringColumns();
    }

    public void printTableStatistics() {
        int total = this.rowStats.size();
        int hasTainted = 0;
        int oneThird = 0;
        int half = 0;
        int twoThirds = 0;
        int all = 0;
        int oneThirdString = 0;
        int halfString = 0;
        int twoThirdsString = 0;
        int allString = 0;
        for (RowStatistics row : this.rowStats) {
            if (row.getTainted() > 0) {
                hasTainted++;
                double perc = row.taintedPercentage();
                if (perc > 99.) {
                    all++;
                }
                if (perc > 66.6) {
                    twoThirds++;
                }
                if (perc > 50.0) {
                    half++;
                }
                if (perc > 33.3) {
                    oneThird++;
                }
                double stringPerc = row.taintedPercentageStringColumns();
                if (stringPerc > 99.) {
                    allString++;
                }
                if (stringPerc > 66.6) {
                    twoThirdsString++;
                }
                if (stringPerc > 50.0) {
                    halfString++;
                }
                if (stringPerc > 33.3) {
                    oneThirdString++;
                }
            }
        }

        if (hasTainted == 0) {
            System.out.printf("%s.%s has no rows with tainted values!%n", this.catalog, this.name);
        } else {
            System.out.printf("%s.%s has %d rows with tainted values!%n", this.catalog, this.name, hasTainted);
            if(oneThirdString > 0) {
                System.out.printf("\tRows with > 33.3%% of tainted String values: %d (Total: %d)%n", oneThirdString, oneThird);
            }
            if(halfString > 0) {
                System.out.printf("\tRows with > 50%% of tainted String values: %d (Total: %d)%n", halfString, half);
            }
            if(twoThirdsString > 0) {
                System.out.printf("\tRows with > 66.6%% of tainted String values: %d (Total: %d)%n", twoThirdsString, twoThirds);
            }
            if(allString > 0) {
                System.out.printf("\tRows with > 99.0%% of tainted String values: %d (Total: %d)%n", allString, all);
            }

        }
    }

}
