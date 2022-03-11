package com.sap.fontus.gdpr.database.defaults;

import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.database.AbstractInformationGatherer;
import com.sap.fontus.gdpr.metadata.DataSubject;
import com.sap.fontus.gdpr.metadata.GdprTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.Collection;

class DefaultValueGatherer extends AbstractInformationGatherer {
    private final String identifier;

    DefaultValueGatherer(String identifier) {
        super();
        this.identifier = identifier;
    }
    @Override
    public void taintedColumn(int index, String name, String type, String value, IASTaintInformationable taintInformation) {
        IASTaintRanges ranges = taintInformation.getTaintRanges(taintInformation.getLength());
        for(IASTaintRange range : ranges) {
            if(!(range.getMetadata() instanceof GdprTaintMetadata)) {
                System.out.printf("In %s.%s, row %d, column %s (%d) the taint has the default taint metadata type%n", this.catalog, this.table, this.row, name, index);

            }
        }
        Collection<DataSubject> dataSubjects = Utils.getDataSubjects(taintInformation);
        for (DataSubject dataSubject : dataSubjects) {
            if (dataSubject.getIdentifier().equals(this.identifier)) {
                System.out.printf("In %s.%s, row %d, column %s (%d) the taint has the default value ('%s') for the data subject!%n", this.catalog, this.table, this.row, name, index, this.identifier);
            }
        }
    }
}
