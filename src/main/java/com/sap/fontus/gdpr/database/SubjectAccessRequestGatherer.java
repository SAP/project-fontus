package com.sap.fontus.gdpr.database;

import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.metadata.DataSubject;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.Collection;

public class SubjectAccessRequestGatherer implements InformationGatherer {
    private final String identifier;
    private String table = "";
    private String catalog = "";
    private int row = 0;

    public SubjectAccessRequestGatherer(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void beginTable(String catalog, String table) {
        this.catalog = catalog;
        this.table = table;
    }

    @Override
    public void endTable() {
        this.catalog = "";
        this.table = "";
        this.row = 0;
    }

    @Override
    public void nextRow() {
        this.row++;
    }

    @Override
    public void taintedColumn(int index, String name, String value, IASTaintInformationable taintInformation) {
        Collection<DataSubject> dataSubjects = Utils.getDataSubjects(taintInformation);
        for(DataSubject dataSubject : dataSubjects) {
            if(dataSubject.getIdentifier().equals(this.identifier)) {
                System.out.printf("In %s.%s, row %d, column %s (%d) has an attached, non empty taint belonging to user: %s!%n", this.catalog, this.table, this.row, name, index, this.identifier);
            }
        }
    }

    @Override
    public void untaintedColumn(int index, String name, Object value) {

    }
}
