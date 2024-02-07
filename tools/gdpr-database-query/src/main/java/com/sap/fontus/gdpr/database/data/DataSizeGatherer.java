package com.sap.fontus.gdpr.database.data;


import com.sap.fontus.gdpr.database.AbstractInformationGatherer;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class DataSizeGatherer extends AbstractInformationGatherer {

    private Map<String, Integer> sizeMap = new HashMap<>();

    @Override
    public void beginTable(String catalog, String table) {
        super.beginTable(catalog, table);
    }

    @Override
    public void endTable() {
        super.endTable();
    }

    @Override
    public void tableSize(int size) {
        super.tableSize(size);
        this.sizeMap.put(this.table, this.size);
    }

    @Override
    public void nextRow() {
        super.nextRow();
    }

    @Override
    public void taintedColumn(int index, String name,String type, String value, IASTaintInformationable taintInformation) {
    }

    @Override
    public void untaintedColumn(int index, String name, String type, Object value) {
    }

    public void printStatistics() {
        int total = 0;
        for (String key: this.sizeMap.keySet()) {
            int rowSize = this.sizeMap.get(key);
            // System.out.println(key + ": " + rowSize);
            if (rowSize > 0) {
                total += rowSize;
            }
        }
        System.out.println("Total DB Size ( B): " + (total));
        System.out.println("Total DB Size (kB): " + (total / 1024));
        System.out.println("Total DB Size (MB): " + (total / 1024 / 1024));
    }
}
