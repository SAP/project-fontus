package com.sap.fontus.gdpr.database;

import com.sap.fontus.taintaware.unified.IASTaintInformationable;

public interface InformationGatherer {
    void beginTable(String catalog, String table);

    void tableSize(long size);

    void endTable();

    void nextRow();

    void taintedColumn(int index, String name, String type, String value, IASTaintInformationable taintInformation);

    void untaintedColumn(int index, String name, String type, Object value);

}
