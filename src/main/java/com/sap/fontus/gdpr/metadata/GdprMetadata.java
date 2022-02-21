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
    // Enables Subject Access Request as per GDPR Article 15
    public DataSubject getSubject();

    // Unique ID to uniquely identify this piece of data
    // Metadatum 4
    // Allows e.g. logging of which data has been sent to whom
    public DataId getId();

    // Is the data portable?
    // Metadatum 5
    // Maps to GDPR Article 20
    // Data requested to be shared by the data subject with a third party
    // All data which is directly input by the user (not those which have been processed)
    public boolean isQualifiedForPortability();

    // Is processing restricted?
    // Metadatum 6
    // Is unrestricted processing allowed on this data?
    // Processing might be restricted e.g. due to inaccurate data, which needs to be corrected.
    // Maps to GDPR Article 18
    public boolean isProcessingUnrestricted();

    // Can the data be used to directly identify a person?
    // Explicit / not explicit
    // Metadatum 7
    public Identifiability isIdentifiable();

}
