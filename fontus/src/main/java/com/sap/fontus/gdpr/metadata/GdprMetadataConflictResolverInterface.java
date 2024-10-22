package com.sap.fontus.gdpr.metadata;

/**
 * Can be called in cases where GdprMetadata should be applied
 * to data which already has GdprMetadata associated with it
 */
public interface GdprMetadataConflictResolverInterface {

    GdprMetadata resolveConflicts(GdprMetadata first, GdprMetadata second);

}
