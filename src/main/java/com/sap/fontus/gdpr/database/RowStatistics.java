package com.sap.fontus.gdpr.database;

class RowStatistics {
    private int untainted = 0;
    private int stringColumns = 0;
    private int tainted = 0;

    public void incrementTainted() {
        this.tainted++;
    }

    public void incrementUntainted() {
        this.untainted++;
    }
    public void incrementStringColumns() {
        this.stringColumns++;
    }

    public int getTainted() {
        return this.tainted;
    }

    public int getUntainted() {
        return this.untainted;
    }

    public int getStringColumns() {
        return this.stringColumns;
    }
    public double taintedPercentageStringColumns() {
        return (double) this.tainted / ((double) this.stringColumns / 100.0);
    }

    public double taintedPercentage() {
        return (double) this.tainted / ((double) (this.untainted+this.tainted) / 100.0);
    }
}
