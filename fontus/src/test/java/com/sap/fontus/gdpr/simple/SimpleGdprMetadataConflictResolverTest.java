package com.sap.fontus.gdpr.simple;

import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SimpleGdprMetadataConflictResolverTest {

    private final Purpose ads = new SimplePurpose(1, "ads", "advertising", "legal text");
    private final Purpose evil = new SimplePurpose(2, "evil", "evil stuff", "legal text");

    private final Vendor acme = new SimpleVendor(1, "acme");
    private final Vendor acu = new SimpleVendor(2, "acu");

    private final ExpiryDate never = new SimpleExpiryDate();
    private final ExpiryDate epoch = new SimpleExpiryDate(Instant.EPOCH);
    private final ExpiryDate now = new SimpleExpiryDate(Instant.now());

    private final DataSubject firstSubject = new SimpleDataSubject();
    private final DataSubject secondSubject = new SimpleDataSubject();

    private final DataId firstDataId = new SimpleDataId();
    private final DataId secondDataId = new SimpleDataId();

    private final GdprMetadataConflictResolverInterface resolver = new SimpleGdprMetadataConflictResolver();

    private GdprMetadata getFirst() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(this.acme);

        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(this.epoch, this.ads, vendors);
        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Normal, this.firstSubject, this.firstDataId, true, true, Identifiability.Explicit);
    }

    private GdprMetadata getSecond() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(this.acu);

        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(this.now, this.evil, vendors);
        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Normal, this.secondSubject, this.firstDataId, true, true, Identifiability.Explicit);
    }


    private GdprMetadata getThird() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(this.acu);

        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(this.now, this.evil, vendors);
        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Sensitive, this.secondSubject, this.firstDataId, false, false, Identifiability.NotExplicit);
    }

    private GdprMetadata getFourth() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(this.acu);

        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(this.now, this.evil, vendors);
        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Undefined, this.secondSubject, this.firstDataId, false, false, Identifiability.Undefined);
    }

    private GdprMetadata getFifth() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(this.acu);
        vendors.add(this.acme);

        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(new SimpleAllowedPurpose(this.now, this.evil, vendors));
        allowedPurposes.add(new SimpleAllowedPurpose(this.now, this.ads, vendors));

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Undefined, this.secondSubject, this.firstDataId, false, false, Identifiability.Undefined);
    }

    private GdprMetadata getSixth() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(this.acu);
        vendors.add(this.acme);

        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(new SimpleAllowedPurpose(this.now, this.evil, vendors));
        allowedPurposes.add(new SimpleAllowedPurpose(this.never, this.ads, vendors));

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Undefined, this.secondSubject, this.firstDataId, false, false, Identifiability.Undefined);
    }

    @Test
    void testConflictResolution() {
        GdprMetadata metadata = this.resolver.resolveConflicts(this.getFirst(), this.getSecond());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(this.firstSubject));
        assertTrue(metadata.getSubjects().contains(this.secondSubject));
        assertNotEquals(this.firstDataId, metadata.getId());
        assertNotEquals(this.secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(0, allowedPurposes.size());

        assertEquals(ProtectionLevel.Normal, metadata.getProtectionLevel());

        assertTrue(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }


    @Test
    void testConflictResolution2() {
        GdprMetadata metadata = this.resolver.resolveConflicts(this.getFirst(), this.getThird());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(this.firstSubject));
        assertTrue(metadata.getSubjects().contains(this.secondSubject));
        assertNotEquals(this.firstDataId, metadata.getId());
        assertNotEquals(this.secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(0, allowedPurposes.size());

        assertEquals(ProtectionLevel.Sensitive, metadata.getProtectionLevel());

        assertFalse(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }

    @Test
    void testConflictResolution3() {
        GdprMetadata metadata = this.resolver.resolveConflicts(this.getFirst(), this.getFourth());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(this.firstSubject));
        assertTrue(metadata.getSubjects().contains(this.secondSubject));
        assertNotEquals(this.firstDataId, metadata.getId());
        assertNotEquals(this.secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(0, allowedPurposes.size());

        assertEquals(ProtectionLevel.Normal, metadata.getProtectionLevel());

        assertFalse(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }

    @Test
    void testConflictResolution4() {
        GdprMetadata metadata = this.resolver.resolveConflicts(this.getFirst(), this.getFifth());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(this.firstSubject));
        assertTrue(metadata.getSubjects().contains(this.secondSubject));
        assertNotEquals(this.firstDataId, metadata.getId());
        assertNotEquals(this.secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(1, allowedPurposes.size());
        AllowedPurpose ap = allowedPurposes.iterator().next();
        assertEquals(this.epoch, ap.getExpiryDate());
        assertEquals(this.ads, ap.getAllowedPurpose());
        assertEquals(1, ap.getAllowedVendors().size());
        assertEquals(this.acme, ap.getAllowedVendors().iterator().next());

        assertEquals(ProtectionLevel.Normal, metadata.getProtectionLevel());

        assertFalse(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }

    @Test
    void testConflictResolution5() {
        GdprMetadata metadata = this.resolver.resolveConflicts(this.getFirst(), this.getSixth());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(this.firstSubject));
        assertTrue(metadata.getSubjects().contains(this.secondSubject));
        assertNotEquals(this.firstDataId, metadata.getId());
        assertNotEquals(this.secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(1, allowedPurposes.size());
        AllowedPurpose ap = allowedPurposes.iterator().next();
        assertEquals(this.epoch, ap.getExpiryDate());
        assertEquals(this.ads, ap.getAllowedPurpose());
        assertEquals(1, ap.getAllowedVendors().size());
        assertEquals(this.acme, ap.getAllowedVendors().iterator().next());

        assertEquals(ProtectionLevel.Normal, metadata.getProtectionLevel());

        assertFalse(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }
}
