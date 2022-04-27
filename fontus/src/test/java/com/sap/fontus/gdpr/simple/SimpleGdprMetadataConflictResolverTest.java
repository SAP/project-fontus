package com.sap.fontus.gdpr.simple;

import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleGdprMetadataConflictResolverTest {

    Purpose ads = new SimplePurpose(1, "ads", "advertising", "legal text");
    Purpose evil = new SimplePurpose(2, "evil", "evil stuff", "legal text");

    Vendor acme = new SimpleVendor(1, "acme");
    Vendor acu = new SimpleVendor(2, "acu");

    ExpiryDate never = new SimpleExpiryDate();
    ExpiryDate epoch = new SimpleExpiryDate(Instant.EPOCH);
    ExpiryDate now = new SimpleExpiryDate(Instant.now());

    DataSubject firstSubject = new SimpleDataSubject();
    DataSubject secondSubject = new SimpleDataSubject();

    DataId firstDataId = new SimpleDataId();
    DataId secondDataId = new SimpleDataId();

    private GdprMetadataConflictResolverInterface resolver = new SimpleGdprMetadataConflictResolver();

    private GdprMetadata getFirst() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(acme);

        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(epoch, ads, vendors);
        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Normal, firstSubject, firstDataId, true, true, Identifiability.Explicit);
    }

    private GdprMetadata getSecond() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(acu);

        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(now, evil, vendors);
        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Normal, secondSubject, firstDataId, true, true, Identifiability.Explicit);
    }


    private GdprMetadata getThird() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(acu);

        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(now, evil, vendors);
        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Sensitive, secondSubject, firstDataId, false, false, Identifiability.NotExplicit);
    }

    private GdprMetadata getFourth() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(acu);

        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(now, evil, vendors);
        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Undefined, secondSubject, firstDataId, false, false, Identifiability.Undefined);
    }

    private GdprMetadata getFifth() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(acu);
        vendors.add(acme);

        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(new SimpleAllowedPurpose(now, evil, vendors));
        allowedPurposes.add(new SimpleAllowedPurpose(now, ads, vendors));

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Undefined, secondSubject, firstDataId, false, false, Identifiability.Undefined);
    }

    private GdprMetadata getSixth() {
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(acu);
        vendors.add(acme);

        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(new SimpleAllowedPurpose(now, evil, vendors));
        allowedPurposes.add(new SimpleAllowedPurpose(never, ads, vendors));

        return new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Undefined, secondSubject, firstDataId, false, false, Identifiability.Undefined);
    }

    @Test
    public void testConflictResolution() {
        GdprMetadata metadata = resolver.resolveConflicts(getFirst(), getSecond());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(firstSubject));
        assertTrue(metadata.getSubjects().contains(secondSubject));
        assertNotEquals(firstDataId, metadata.getId());
        assertNotEquals(secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(0, allowedPurposes.size());

        assertEquals(ProtectionLevel.Normal, metadata.getProtectionLevel());

        assertTrue(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }


    @Test
    public void testConflictResolution2() {
        GdprMetadata metadata = resolver.resolveConflicts(getFirst(), getThird());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(firstSubject));
        assertTrue(metadata.getSubjects().contains(secondSubject));
        assertNotEquals(firstDataId, metadata.getId());
        assertNotEquals(secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(0, allowedPurposes.size());

        assertEquals(ProtectionLevel.Sensitive, metadata.getProtectionLevel());

        assertFalse(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }

    @Test
    public void testConflictResolution3() {
        GdprMetadata metadata = resolver.resolveConflicts(getFirst(), getFourth());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(firstSubject));
        assertTrue(metadata.getSubjects().contains(secondSubject));
        assertNotEquals(firstDataId, metadata.getId());
        assertNotEquals(secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(0, allowedPurposes.size());

        assertEquals(ProtectionLevel.Normal, metadata.getProtectionLevel());

        assertFalse(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }

    @Test
    public void testConflictResolution4() {
        GdprMetadata metadata = resolver.resolveConflicts(getFirst(), getFifth());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(firstSubject));
        assertTrue(metadata.getSubjects().contains(secondSubject));
        assertNotEquals(firstDataId, metadata.getId());
        assertNotEquals(secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(1, allowedPurposes.size());
        AllowedPurpose ap = allowedPurposes.iterator().next();
        assertEquals(epoch, ap.getExpiryDate());
        assertEquals(ads, ap.getAllowedPurpose());
        assertEquals(1, ap.getAllowedVendors().size());
        assertEquals(acme, ap.getAllowedVendors().iterator().next());

        assertEquals(ProtectionLevel.Normal, metadata.getProtectionLevel());

        assertFalse(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }

    @Test
    public void testConflictResolution5() {
        GdprMetadata metadata = resolver.resolveConflicts(getFirst(), getSixth());

        // Should always apply
        assertTrue(metadata.getSubjects().contains(firstSubject));
        assertTrue(metadata.getSubjects().contains(secondSubject));
        assertNotEquals(firstDataId, metadata.getId());
        assertNotEquals(secondDataId, metadata.getId());
        assertFalse(metadata.isQualifiedForPortability());

        // Test specific stuff
        Collection<AllowedPurpose> allowedPurposes = metadata.getAllowedPurposes();
        assertEquals(1, allowedPurposes.size());
        AllowedPurpose ap = allowedPurposes.iterator().next();
        assertEquals(epoch, ap.getExpiryDate());
        assertEquals(ads, ap.getAllowedPurpose());
        assertEquals(1, ap.getAllowedVendors().size());
        assertEquals(acme, ap.getAllowedVendors().iterator().next());

        assertEquals(ProtectionLevel.Normal, metadata.getProtectionLevel());

        assertFalse(metadata.isProcessingUnrestricted());

        assertEquals(Identifiability.Explicit, metadata.isIdentifiable());
    }
}
