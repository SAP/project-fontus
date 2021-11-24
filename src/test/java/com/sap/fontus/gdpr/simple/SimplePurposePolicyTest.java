package com.sap.fontus.gdpr.simple;

import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimplePurposePolicyTest {

    private Purpose p1 = new SimplePurpose(1, "logging");
    private Purpose p2 = new SimplePurpose(2, "storage");
    private Purpose p3 = new SimplePurpose(2, "profiling");

    private Vendor v1 = new SimpleVendor(1, "Acme");
    private Vendor v2 = new SimpleVendor(2, "Bodgit Ltd.");
    private Vendor v3 = new SimpleVendor(3, "E-Corp");

    @Test
    public void purposePolicyTestOk() {

        SimpleRequiredPurposes required = new SimpleRequiredPurposes();
        // Required Vendors
        required.addVendor(v1);
        // Required Purposes
        required.addPurpose(p1);

        Collection<AllowedPurpose> allowed = new ArrayList<>();
        Set<Vendor> allowedVendors = new HashSet<>();

        // Allowed Vendors
        allowedVendors.add(v1);
        // Allowed Purpose
        allowed.add(new SimpleAllowedPurpose(new SimpleExpiryDate(), p1, allowedVendors));

        PurposePolicy policy = new SimplePurposePolicy();
        assertTrue(policy.areRequiredPurposesAllowed(required, allowed));
    }

    @Test
    public void purposePolicyTestPurposeNotAllowed() {

        SimpleRequiredPurposes required = new SimpleRequiredPurposes();
        // Required Vendors
        required.addVendor(v1);
        // Required Purposes
        required.addPurpose(p2);

        Collection<AllowedPurpose> allowed = new ArrayList<>();
        Set<Vendor> allowedVendors = new HashSet<>();

        // Allowed Vendors
        allowedVendors.add(v1);
        // Allowed Purpose
        allowed.add(new SimpleAllowedPurpose(new SimpleExpiryDate(), p1, allowedVendors));

        PurposePolicy policy = new SimplePurposePolicy();
        assertFalse(policy.areRequiredPurposesAllowed(required, allowed));
    }

    @Test
    public void purposePolicyTestOneOfTwoPurposesNotAllowed() {
        SimpleRequiredPurposes required = new SimpleRequiredPurposes();
        // Required Vendors
        required.addVendor(v1);
        // Required Purposes
        required.addPurpose(p1);
        required.addPurpose(p2);

        Collection<AllowedPurpose> allowed = new ArrayList<>();
        Set<Vendor> allowedVendors = new HashSet<>();

        // Allowed Vendors
        allowedVendors.add(v1);
        // Allowed Purpose
        allowed.add(new SimpleAllowedPurpose(new SimpleExpiryDate(), p1, allowedVendors));

        PurposePolicy policy = new SimplePurposePolicy();
        assertFalse(policy.areRequiredPurposesAllowed(required, allowed));
    }

    @Test
    public void purposePolicyTestTwoPurposesAllowed() {
        SimpleRequiredPurposes required = new SimpleRequiredPurposes();
        // Required Vendors
        required.addVendor(v1);
        // Required Purposes
        required.addPurpose(p1);
        required.addPurpose(p2);

        Collection<AllowedPurpose> allowed = new ArrayList<>();
        Set<Vendor> allowedVendors = new HashSet<>();

        // Allowed Vendors
        allowedVendors.add(v1);
        // Allowed Purpose
        allowed.add(new SimpleAllowedPurpose(new SimpleExpiryDate(), p1, allowedVendors));
        allowed.add(new SimpleAllowedPurpose(new SimpleExpiryDate(), p2, allowedVendors));

        PurposePolicy policy = new SimplePurposePolicy();
        assertTrue(policy.areRequiredPurposesAllowed(required, allowed));
    }

    @Test
    public void purposePolicyTestVendorNotAllowed() {

        SimpleRequiredPurposes required = new SimpleRequiredPurposes();
        // Required Vendors
        required.addVendor(v2);
        // Required Purposes
        required.addPurpose(p2);

        Collection<AllowedPurpose> allowed = new ArrayList<>();
        Set<Vendor> allowedVendors = new HashSet<>();

        // Allowed Vendors
        allowedVendors.add(v1);
        // Allowed Purpose
        allowed.add(new SimpleAllowedPurpose(new SimpleExpiryDate(), p1, allowedVendors));

        PurposePolicy policy = new SimplePurposePolicy();
        assertFalse(policy.areRequiredPurposesAllowed(required, allowed));
    }

    @Test
    public void purposePolicyTestVendorTooManyVendors() {

        SimpleRequiredPurposes required = new SimpleRequiredPurposes();
        // Required Vendors
        required.addVendor(v1);
        required.addVendor(v2);
        // Required Purposes
        required.addPurpose(p2);

        Collection<AllowedPurpose> allowed = new ArrayList<>();
        Set<Vendor> allowedVendors = new HashSet<>();

        // Allowed Vendors
        allowedVendors.add(v1);
        // Allowed Purpose
        allowed.add(new SimpleAllowedPurpose(new SimpleExpiryDate(), p1, allowedVendors));

        PurposePolicy policy = new SimplePurposePolicy();
        assertFalse(policy.areRequiredPurposesAllowed(required, allowed));
    }

    @Test
    public void purposePolicyTestVendorTooManyVendorsOk() {

        SimpleRequiredPurposes required = new SimpleRequiredPurposes();
        // Required Vendors
        required.addVendor(v1);
        required.addVendor(v2);
        // Required Purposes
        required.addPurpose(p1);

        Collection<AllowedPurpose> allowed = new ArrayList<>();
        Set<Vendor> allowedVendors = new HashSet<>();

        // Allowed Vendors
        allowedVendors.add(v1);
        allowedVendors.add(v2);
        // Allowed Purpose
        allowed.add(new SimpleAllowedPurpose(new SimpleExpiryDate(), p1, allowedVendors));

        PurposePolicy policy = new SimplePurposePolicy();
        assertTrue(policy.areRequiredPurposesAllowed(required, allowed));
    }
}
