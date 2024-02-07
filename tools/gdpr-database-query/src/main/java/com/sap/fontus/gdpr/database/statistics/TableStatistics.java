package com.sap.fontus.gdpr.database.statistics;

import java.util.ArrayList;
import java.util.Collection;

class TableStatistics {

    private final String catalog;
    private final String name;
    private final Collection<RowStatistics> rowStats = new ArrayList<>();
    private RowStatistics currentRow = null;

    TableStatistics(String catalog, String name) {
        this.catalog = catalog;
        this.name = name;
    }

    void nextRow() {
        if (this.currentRow != null) {
            this.rowStats.add(this.currentRow);
        }
        this.currentRow = new RowStatistics();
    }

    void endTable() {
        if (this.currentRow != null) {
            this.rowStats.add(this.currentRow);
        }
        this.currentRow = null;
    }

    void incrementTainted() {
        this.currentRow.incrementTainted();
    }

    void incrementUntainted() {
        this.currentRow.incrementUntainted();
    }

    void incrementStringColumn() {
        this.currentRow.incrementStringColumns();
    }

    void printTableStatistics() {
        int total = this.rowStats.size();
        if(total == 0) {
            return;
        }
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
                if (perc > 99.0) {
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
                if (stringPerc > 99.0) {
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
            System.out.printf("%s.%s has no rows (of %d) with tainted values!%n", this.catalog, this.name, total);
        } else {
            System.out.printf("%s.%s has %d rows (of %d) with tainted values!%n", this.catalog, this.name, hasTainted, total);
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
