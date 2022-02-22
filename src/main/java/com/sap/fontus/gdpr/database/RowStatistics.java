package com.sap.fontus.gdpr.database;

class RowStatistics {
    private int untainted = 0;
    private int tainted = 0;

    public void incrementTainted() {
        this.tainted++;
    }

    public void incrementUntainted() {
        this.untainted++;
    }

    public int getTainted() {
        return this.tainted;
    }

    public int getUntainted() {
        return this.untainted;
    }

    public double taintedPercentage() {
        return (double) this.tainted / ((double) this.untainted / 100.0);
    }
}
