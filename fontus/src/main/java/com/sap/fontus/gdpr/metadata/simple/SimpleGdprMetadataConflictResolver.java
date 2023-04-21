package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SimpleGdprMetadataConflictResolver implements GdprMetadataConflictResolverInterface, Serializable {

    public SimpleGdprMetadataConflictResolver() {

    }

    private Comparable getMax(Comparable first, Comparable second) {
        if (first.compareTo(second) >= 0) {
            return first;
        }
        return second;
    }

    private Comparable getMin(Comparable first, Comparable<ExpiryDate> second) {
        if (first.compareTo(second) <= 0) {
            return first;
        }
        return second;
    }

    private Identifiability combineIdentifiability(Identifiability first, Identifiability second) {
        return (Identifiability) this.getMax(first, second);
    }

    private ProtectionLevel combineProtectionLevels(ProtectionLevel first, ProtectionLevel second) {
        return (ProtectionLevel) this.getMax(first, second);
    }

    private Collection<AllowedPurpose> combineAllowedPurposes(Collection<AllowedPurpose> first, Collection<AllowedPurpose> second) {
        Collection<AllowedPurpose> allowedPurposes = new HashSet<>();
        // Intersection...
        for (AllowedPurpose p1 : first) {
            for (AllowedPurpose p2 : second) {
                if (p1.getAllowedPurpose().equals(p2.getAllowedPurpose())) {
                    // The soonest expiry date
                    ExpiryDate expiryDate = (ExpiryDate) this.getMin(p1.getExpiryDate(), p2.getExpiryDate());
                    // Intersection between sets
                    Set<Vendor> vendors = new HashSet<>(p1.getAllowedVendors());
                    vendors.retainAll(p2.getAllowedVendors());
                    AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(expiryDate, p1.getAllowedPurpose(), vendors);
                    allowedPurposes.add(allowedPurpose);
                }
            }
        }
        return allowedPurposes;
    }

    @Override
    public GdprMetadata resolveConflicts(GdprMetadata first, GdprMetadata second) {

        // Metadatum 1
        // Purposes, Recipients -> intersection
        // Expiry -> minimum
        Collection<AllowedPurpose> allowedPurposes = this.combineAllowedPurposes(first.getAllowedPurposes(), second.getAllowedPurposes());

        // Metadatum 2
        // Maximum
        ProtectionLevel protectionLevel = this.combineProtectionLevels(first.getProtectionLevel(), second.getProtectionLevel());

        // Metadatum 3
        // Union
        Collection<DataSubject> dataSubjects = new HashSet<>(first.getSubjects());
        dataSubjects.addAll(second.getSubjects());

        // Metadatum 4
        // Data ID -> Regenerate
        DataId newId = new SimpleDataId();

        // Metadatum 5
        // Value always no
        boolean isQualifiedForPortability = false;

        // Metadatum 6
        // isProcessingUnrestricted -> AND
        boolean isProcessingUnrestricted = first.isProcessingUnrestricted() && second.isProcessingUnrestricted();

        // Metadatum 7
        // OR
        Identifiability isIdentifiable = this.combineIdentifiability(first.isIdentifiable(), second.isIdentifiable());

        return new SimpleGdprMetadata(allowedPurposes, protectionLevel, dataSubjects, newId, isQualifiedForPortability, isProcessingUnrestricted, isIdentifiable);
    }

}
