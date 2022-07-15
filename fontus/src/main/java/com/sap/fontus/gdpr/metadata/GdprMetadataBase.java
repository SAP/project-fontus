package com.sap.fontus.gdpr.metadata;

public abstract class GdprMetadataBase implements GdprMetadata {

    @Override
    public String toString() {
        return "SimpleGdprMetadata{" +
                "allowedPurposes=" + this.getAllowedPurposes() +
                ", protectionLevel=" + this.getProtectionLevel() +
                ", dataSubject=" + this.getSubjects() +
                ", dataId=" + this.getId() +
                ", portability=" + this.isQualifiedForPortability() +
                ", consent=" + this.isProcessingUnrestricted() +
                ", identifiability=" + this.isIdentifiable() +
                '}';
    }
}
