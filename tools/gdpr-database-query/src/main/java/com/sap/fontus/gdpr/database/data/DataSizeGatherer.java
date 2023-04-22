package com.sap.fontus.gdpr.database.data;


import com.sap.fontus.gdpr.database.AbstractInformationGatherer;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class DataSizeGatherer extends AbstractInformationGatherer {

    private Map<String, Long> sizeMap = new HashMap<>();

    @Override
    public void beginTable(String catalog, String table) {
        super.beginTable(catalog, table);
    }

    @Override
    public void endTable() {
        super.endTable();
    }

    @Override
    public void tableSize(long size) {
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
        long total = 0L;
        for (String key: this.sizeMap.keySet()) {
            long rowSize = this.sizeMap.get(key);
            // System.out.println(key + ": " + rowSize);
            if (rowSize > 0L) {
                total += rowSize;
            }
        }
        System.out.println("Total DB Size ( B): " + (total));
        System.out.println("Total DB Size (kB): " + (total / 1024L));
        System.out.println("Total DB Size (MB): " + (total / 1024L / 1024L));
    }
}
