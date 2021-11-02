package com.sap.fontus.gdpr.metadata;

import java.util.List;
import java.util.Objects;

public class GdprMetadata {

    private List<Purpose> allowedPurposes;

    private ProtectionLevel protectionLevel;

    private DataSubject subject;

    private Identifiability identifiability;

    private boolean qualifiedForPortability;

    private boolean processingRestricted;

    public GdprMetadata(List<Purpose> allowedPurposes, ProtectionLevel protectionLevel, DataSubject subject,
                        Identifiability identifiability, boolean qualifiedForPortability, boolean processingRestricted) {
        this.allowedPurposes = allowedPurposes;
        this.protectionLevel = protectionLevel;
        this.subject = subject;
        this.identifiability = identifiability;
        this.qualifiedForPortability = qualifiedForPortability;
        this.processingRestricted = processingRestricted;
    }

    public GdprMetadata() {

    }

    public boolean checkAllowedPurpose(Purpose purpose) {
        return allowedPurposes.contains(purpose);
    }

    public void addAllowedPurpose(Purpose purpose) {
        allowedPurposes.add(purpose);
    }

    public List<Purpose> getAllowedPurposes() {
        return allowedPurposes;
    }

    public void setAllowedPurposes(List<Purpose> allowedPurposes) {
        this.allowedPurposes = allowedPurposes;
    }

    public ProtectionLevel getProtectionLevel() {
        return protectionLevel;
    }

    public void setProtectionLevel(ProtectionLevel protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    public DataSubject getSubject() {
        return subject;
    }

    public void setSubject(DataSubject subject) {
        this.subject = subject;
    }

    public Identifiability getIdentifiability() {
        return identifiability;
    }

    public void setIdentifiability(Identifiability identifiability) {
        this.identifiability = identifiability;
    }

    public boolean isQualifiedForPortability() {
        return qualifiedForPortability;
    }

    public void setQualifiedForPortability(boolean qualifiedForPortability) {
        this.qualifiedForPortability = qualifiedForPortability;
    }

    public boolean isProcessingRestricted() {
        return processingRestricted;
    }

    public void setProcessingRestricted(boolean processingRestricted) {
        this.processingRestricted = processingRestricted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GdprMetadata that = (GdprMetadata) o;
        return qualifiedForPortability == that.qualifiedForPortability && processingRestricted == that.processingRestricted && Objects.equals(allowedPurposes, that.allowedPurposes) && Objects.equals(protectionLevel, that.protectionLevel) && Objects.equals(subject, that.subject) && Objects.equals(identifiability, that.identifiability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedPurposes, protectionLevel, subject, identifiability, qualifiedForPortability, processingRestricted);
    }

    @Override
    public String toString() {
        return "GdprMetadata{" +
                "allowedPurposes=" + allowedPurposes +
                ", protectionLevel=" + protectionLevel +
                ", subject=" + subject +
                ", identifiability=" + identifiability +
                ", qualifiedForPortability=" + qualifiedForPortability +
                ", processingRestricted=" + processingRestricted +
                '}';
    }
}
