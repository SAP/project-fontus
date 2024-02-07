package com.sap.fontus.gdpr.database.accessrequest;

import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.database.AbstractInformationGatherer;
import com.sap.fontus.gdpr.metadata.DataSubject;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.Collection;

class SubjectAccessRequestGatherer extends AbstractInformationGatherer {
    private final String identifier;

    SubjectAccessRequestGatherer(String identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public void taintedColumn(int index, String name, String type, String value, IASTaintInformationable taintInformation) {
        Collection<DataSubject> dataSubjects = Utils.getDataSubjects(taintInformation);
        for (DataSubject dataSubject : dataSubjects) {
            if (dataSubject.getIdentifier().equals(this.identifier)) {
                System.out.printf("In %s.%s, row %d, column %s (%d) has an attached, non empty taint belonging to user: %s!%n", this.catalog, this.table, this.row, name, index, this.identifier);
            }
        }
    }

}
