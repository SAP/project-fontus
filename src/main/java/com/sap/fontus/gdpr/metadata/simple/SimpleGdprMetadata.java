package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class SimpleGdprMetadata extends GdprMetadataBase {

    // TODO(david): I have marked a bunch of them as final -- are there any more that can change values at runtime?
    private final Collection<AllowedPurpose> allowedPurposes;
    private ProtectionLevel protectionLevel;
    private final DataSubject dataSubject;
    private final DataId dataId;
    private final boolean portability;
    private boolean processingUnrestricted;
    private final Identifiability identifiability;

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
        return this.allowedPurposes;
    }

    @Override
    public ProtectionLevel getProtectionLevel() {
        return this.protectionLevel;
    }

    @Override
    public DataSubject getSubject() {
        return this.dataSubject;
    }

    @Override
    public DataId getId() {
        return this.dataId;
    }

    @Override
    public boolean isQualifiedForPortability() {
        return this.portability;
    }

    @Override
    public boolean isProcessingUnrestricted() {
        return this.processingUnrestricted;
    }

    @Override
    public Identifiability isIdentifiable() {
        return this.identifiability;
    }

    @Override
    public void setProtectionLevel(ProtectionLevel protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    @Override
    public void restrictProcessing() {
        this.processingUnrestricted = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SimpleGdprMetadata that = (SimpleGdprMetadata) o;
        return this.portability == that.portability
                && this.processingUnrestricted == that.processingUnrestricted
                && Objects.equals(this.allowedPurposes, that.allowedPurposes)
                && this.protectionLevel == that.protectionLevel
                && Objects.equals(this.dataSubject, that.dataSubject)
                && Objects.equals(this.dataId, that.dataId)
                && this.identifiability == that.identifiability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.allowedPurposes, this.protectionLevel, this.dataSubject, this.dataId, this.portability, this.processingUnrestricted, this.identifiability);
    }

}
