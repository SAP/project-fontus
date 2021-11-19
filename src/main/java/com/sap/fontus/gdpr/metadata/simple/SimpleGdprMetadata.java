package com.sap.fontus.gdpr.metadata.simple;

import com.sap.fontus.gdpr.metadata.*;

import java.util.Objects;
import java.util.Set;

public class SimpleGdprMetadata extends GdprMetadataBase {

    private Set<AllowedPurpose> allowedPurposes;
    private ProtectionLevel protectionLevel;
    private DataSubject dataSubject;
    private DataId dataId;
    private boolean portability;
    private boolean consent;
    private Identifiability identifiability;

    public SimpleGdprMetadata(Set<AllowedPurpose> allowedPurposes, ProtectionLevel protectionLevel, DataSubject dataSubject, DataId dataId, boolean portability, boolean consent, Identifiability identifiability) {
        this.allowedPurposes = allowedPurposes;
        this.protectionLevel = protectionLevel;
        this.dataSubject = dataSubject;
        this.dataId = dataId;
        this.portability = portability;
        this.consent = consent;
        this.identifiability = identifiability;
    }

    @Override
    public Set<AllowedPurpose> getAllowedPurposes() {
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
    public boolean isConsentGiven() {
        return consent;
    }

    @Override
    public Identifiability isIdentifiabible() {
        return identifiability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleGdprMetadata that = (SimpleGdprMetadata) o;
        return portability == that.portability && consent == that.consent && Objects.equals(allowedPurposes, that.allowedPurposes) && protectionLevel == that.protectionLevel && Objects.equals(dataSubject, that.dataSubject) && Objects.equals(dataId, that.dataId) && identifiability == that.identifiability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedPurposes, protectionLevel, dataSubject, dataId, portability, consent, identifiability);
    }

}
