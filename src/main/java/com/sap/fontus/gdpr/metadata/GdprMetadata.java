package com.sap.fontus.gdpr.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface GdprMetadata {

    // List of allowed purposes for a given receiver, together with expiry information
    // Metadatum 1
    // TCF - getAllowedPurposes and getAllowedVendorIds
    public Collection<AllowedPurpose> getAllowedPurposes();

    // Level of protection required, e.g. sensitive medical data
    // Metadatum 2
    public ProtectionLevel getProtectionLevel();

    // The data subject needs to be identified
    // Metadatum 3
    public DataSubject getSubject();

    // Unique ID to uniquely identify this piece of data
    // Metadatum 4
    public DataId getId();

    // Is the data portable?
    // Metadatum 5
    public boolean isQualifiedForPortability();

    // Was consent explicitly given to use this data
    // Metadatum 6
    // TCF - getConsentScreen()
    public boolean isConsentGiven();

    // Can the data be used to directly identify a person?
    // Explicit / not explicit
    // Metadatum 7
    public Identifiability isIdentifiabible();

}
