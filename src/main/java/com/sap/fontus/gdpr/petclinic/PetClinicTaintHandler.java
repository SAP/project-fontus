package com.sap.fontus.gdpr.petclinic;

import com.iabtcf.decoder.TCString;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.*;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.gdpr.tcf.TcfBackedGdprMetadata;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import com.sap.fontus.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetClinicTaintHandler extends IASTaintHandler {

    private static final Pattern addPetsPattern = Pattern.compile("\\/owners\\/([0-9]+)\\/pets\\/new");

    private static GdprMetadata getMetadataFromRequest(ReflectedHttpServletRequest servlet) {
        // TODO: Check flag of request for consent...

        // Create some metadata from the name
        String subjectName = servlet.getParameter("firstName") + servlet.getParameter("lastName");
        DataSubject dataSubject = new SimpleDataSubject(subjectName);

        Purpose purpose = new SimplePurpose(1, "Process and Store", "Allow process and Storage", "");
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(new SimpleVendor(1));
        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(new SimpleExpiryDate(), purpose, vendors);

        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);

        GdprMetadata metadata = new SimpleGdprMetadata(allowedPurposes, ProtectionLevel.Normal, dataSubject,
                new SimpleDataId(), true, true, Identifiability.Explicit);

        return metadata;
    }
    /**
     * Extracts the TCF consent string from a cookie and attaches it as the taint metadata
     * @param taintAware The Taint Aware String-like object
     * @param parent The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {

        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);

        // This might not work as we relocate the HttpServletRequest object...
        ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(parent);

        // Debugging
        System.out.println("Servlet: " + request);
        System.out.println("Request Attributes:");
        ReflectedOwnerRepository.dumpRequestContextHolder();
        System.out.println("Stack Trace:");
        Utils.printCurrentStackTrace();

        // Write the taint policy by hand
        IASString uri = request.getRequestURI();

        if (uri != null) {
            String path = uri.getString();
            if (path.equals("/owners/new")) {
                // New owner
                GdprMetadata metadata = getMetadataFromRequest(request);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
            } else if (path.matches("\\/owners\\/[0-9]+\\/edit")) {
                // Update owner
                GdprMetadata metadata = getMetadataFromRequest(request);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
            } else {
                Matcher m = addPetsPattern.matcher(path);
                if (m.find()) {
                    // See if we can retrieve original the name using the PetClinic interface...
                    String id_match = m.group(1);
                    // Let it throw...
                    int id = Integer.valueOf(id_match);
                    // Can we get the Owner object corresponding to this?


                    GdprMetadata metadata = getMetadataFromRequest(request);
                    taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
                }
            }
        }

        return taintAware;
    }

    /**
     * The taint method can be used as a taintHandler for a given taint source
     * @param object The object to be tainted
     * @param sourceId The ID of the taint source function
     * @return The tainted object
     *
     * This snippet of XML can be added to the source:
     *
     * <tainthandler>
     *     <opcode>184</opcode>
     *     <owner>com/sap/fontus/gdpr/GdprTaintHandler/handler</owner>
     *     <name>taint</name>
     *     <descriptor>(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;I)Ljava/lang/Object;</descriptor>
     *     <interface>false</interface>
     * </tainthandler>
     *
     */
    public static Object taint(Object object, Object parent, Object[] parameters, int sourceId) {
        if (object instanceof IASTaintAware) {
            return setTaint((IASTaintAware) object, parent, parameters, sourceId);
        }
        return IASTaintHandler.traverseObject(object, taintAware -> setTaint(taintAware, parent, parameters, sourceId));
    }
}
