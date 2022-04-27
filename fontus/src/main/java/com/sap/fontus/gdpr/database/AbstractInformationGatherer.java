package com.sap.fontus.gdpr.database;

import com.sap.fontus.taintaware.unified.IASTaintInformationable;

public class AbstractInformationGatherer implements InformationGatherer {
    protected String table = "";
    protected String catalog = "";
    protected int row = 0;

    @Override
    public void beginTable(String catalog, String table) {
        this.row = 0;
        this.catalog = catalog;
        this.table = table;
    }

    @Override
    public void endTable() {
        this.row = 0;
        this.catalog = "";
        this.table = "";
    }

    @Override
    public void nextRow() {
        this.row++;
    }

    @Override
    public void taintedColumn(int index, String name, String type, String value, IASTaintInformationable taintInformation) {
        // NOP
    }

    @Override
    public void untaintedColumn(int index, String name, String type, Object value) {
        // NOP
    }
}
