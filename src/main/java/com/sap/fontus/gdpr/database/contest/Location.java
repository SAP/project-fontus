package com.sap.fontus.gdpr.database.contest;

import com.sap.fontus.sql.tainter.Utils;

public class Location {
    private final String table;
    private final String column;
    private final String idColumn;

    public Location(String idColumn, String table, String column) {
        this.idColumn = idColumn;
        this.table = table;
        this.column = column;
    }

    public String getIdColumn() {
        return this.idColumn;
    }

    public String getColumn() {
        return this.column;
    }

    public String getTaintColumn() {
        return Utils.taintColumnName(this.column);
    }
    public String getTable() {
        return this.table;
    }
}
