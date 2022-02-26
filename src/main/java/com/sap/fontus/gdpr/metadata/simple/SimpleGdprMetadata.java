package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class SimpleGdprMetadata extends GdprMetadataBase {

    private Collection<AllowedPurpose> allowedPurposes;
    private ProtectionLevel protectionLevel;
    private DataSubject dataSubject;
    private DataId dataId;
    private boolean portability;
    private boolean processingUnrestricted;
    private Identifiability identifiability;

    public SimpleGdprMetadata() {
        this.allowedPurposes = Set.of();
        this.protectionLevel = ProtectionLevel.Undefined;
        this.dataSubject = null;
        this.dataId = null;
        this.portability = false;
        this.processingUnrestricted = false;
        this.identifiability = Identifiability.Undefined;
    }

    public SimpleGdprMetadata(Collection<AllowedPurpose> allowedPurposes, ProtectionLevel protectionLevel, DataSubject dataSubject, DataId dataId, boolean portability, boolean consent, Identifiability identifiability) {
        this.allowedPurposes = allowedPurposes;
        this.protectionLevel = protectionLevel;
        this.dataSubject = dataSubject;
        this.dataId = dataId;
        this.portability = portability;
        this.processingUnrestricted = consent;
        this.identifiability = identifiability;
    }

    @Override
    public Collection<AllowedPurpose> getAllowedPurposes() {
        return allowedPurposes;
    }

    @Override
    public ProtectionLevel getProtectionLevel() {
        return protectionLevel;
    }

    @Override
    public DataSubject getSubject() {
        return dataSubject;
    }

    @Override
    public DataId getId() {
        return dataId;
    }

    @Override
    public boolean isQualifiedForPortability() {
        return portability;
    }

    @Override
    public boolean isProcessingUnrestricted() {
        return processingUnrestricted;
    }

    @Override
    public Identifiability isIdentifiable() {
        return identifiability;
    }

    @Override
    public void setProtectionLevel(ProtectionLevel protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleGdprMetadata that = (SimpleGdprMetadata) o;
        return portability == that.portability && processingUnrestricted == that.processingUnrestricted && Objects.equals(allowedPurposes, that.allowedPurposes) && protectionLevel == that.protectionLevel && Objects.equals(dataSubject, that.dataSubject) && Objects.equals(dataId, that.dataId) && identifiability == that.identifiability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedPurposes, protectionLevel, dataSubject, dataId, portability, processingUnrestricted, identifiability);
    }

}
