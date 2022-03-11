package com.sap.fontus.gdpr.database.statistics;


import com.sap.fontus.gdpr.database.AbstractInformationGatherer;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.ArrayList;
import java.util.Collection;

class StatisticsGatherer extends AbstractInformationGatherer {
    private final Collection<TableStatistics> tableStatistics = new ArrayList<>();
    private TableStatistics currentTable = null;

    @Override
    public void beginTable(String catalog, String table) {
        super.beginTable(catalog, table);
        this.currentTable = new TableStatistics(catalog, table);
    }

    @Override
    public void endTable() {
        super.endTable();
        this.currentTable.endTable();
        this.tableStatistics.add(this.currentTable);
        this.currentTable = null;
    }

    @Override
    public void nextRow() {
        super.nextRow();
        this.currentTable.nextRow();
    }

    @Override
    public void taintedColumn(int index, String name,String type, String value, IASTaintInformationable taintInformation) {
        if(isTextType(type)) {
            this.currentTable.incrementStringColumn();
        }
        this.currentTable.incrementTainted();
    }

    @Override
    public void untaintedColumn(int index, String name, String type, Object value) {
        if(isTextType(type)) {
            this.currentTable.incrementStringColumn();
        }
        this.currentTable.incrementUntainted();
    }

    public void printStatistics() {
        for (TableStatistics statistics : this.tableStatistics) {
            statistics.printTableStatistics();
        }
    }

    public static boolean isTextType(String type) {
        switch (type) {
            case "VARCHAR":
            case "MEDIUMTEXT":
            case "TEXT":
            case "LONGTEXT":
                return true;
            default:
                return false;
        }
    }
}
