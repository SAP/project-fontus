package com.sap.fontus.gdpr.petclinic;

import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.*;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import com.sap.fontus.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetClinicTaintHandler extends IASTaintHandler {

    private static final Pattern addNewPetsPattern = Pattern.compile("\\/owners\\/([0-9]+)\\/pets\\/new");
    private static final Pattern editPetsPattern = Pattern.compile("\\/owners\\/([0-9]+)\\/pets\\/([0-9]+)\\/edit");
    private static final Pattern addVisitPattern = Pattern.compile("\\/owners\\/([0-9]+)\\/pets\\/([0-9]+)\\/visits\\/new");

    private static Set<AllowedPurpose> getPurposesFromRequest(ReflectedHttpServletRequest servlet) {
        Purpose purpose = new SimplePurpose(1, "Process and Store", "Allow process and Storage", "");
        Set<Vendor> vendors = new HashSet<>();
        vendors.add(new SimpleVendor(1));
        AllowedPurpose allowedPurpose = new SimpleAllowedPurpose(new SimpleExpiryDate(), purpose, vendors);

        Set<AllowedPurpose> allowedPurposes = new HashSet<>();
        allowedPurposes.add(allowedPurpose);
        return allowedPurposes;
    }

    private static GdprMetadata getMetadataFromOwnerRequest(ReflectedHttpServletRequest servlet) {
        // TODO: Check flag of request for consent...

        // Create some metadata from the name
        String subjectName = servlet.getParameter("firstName") + " " + servlet.getParameter("lastName");
        DataSubject dataSubject = new SimpleDataSubject(subjectName);

        GdprMetadata metadata = new SimpleGdprMetadata(getPurposesFromRequest(servlet), ProtectionLevel.Normal, dataSubject,
                new SimpleDataId(), true, true, Identifiability.Explicit);

        return metadata;
    }

    private static String getNameFromRequest(ReflectedHttpServletRequest servlet, int id) {
        String name = null;
        try {
            // Spring stores the application context in the HttpRequest attributes
            // Should be a org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext
            Object obj = servlet.getAttribute(new IASString("org.springframework.web.servlet.DispatcherServlet.CONTEXT"));
            // Through this context we can access each of the Spring Beans
            Method m = obj.getClass().getMethod("getBean", IASString.class);
            // The PetClinic has a bean called ownerRepository, with class OwnerRepository
            Object bean = m.invoke(obj, new IASString("ownerRepository"));

            // This interface will be proxied by Fontus, but the method names are the samn
            Method m2 = bean.getClass().getMethod("findById", Integer.class);
            Object owner = m2.invoke(bean, Integer.valueOf(id));

            // Now we can extract information about the to create the ID:
            Method m3 = owner.getClass().getMethod("getFirstName");
            Object n = m3.invoke(owner);
            IASString s = (IASString) n;

            Method m4 = owner.getClass().getMethod("getLastName");
            Object n2 = m4.invoke(owner);
            IASString s2 = (IASString) n2;

            name = s.getString() + " " + s2.getString();

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        System.out.println("Found id = " + id + " with name: " + name);
        return name;
    }

    private static DataSubject getDataSubjectFromRequest(ReflectedHttpServletRequest servlet, int id) {
        return new SimpleDataSubject(getNameFromRequest(servlet, id));
    }

    private static GdprMetadata getMetadataFromPetRequest(ReflectedHttpServletRequest servlet, Matcher m, String path, ProtectionLevel protectionLevel) {
        GdprMetadata metadata = null;
        if (addNewPetsPattern.matcher(path).find()) {
            // See if we can retrieve original the name using the PetClinic interface...
            String id_match = m.group(1);
            // Let it throw...
            int id = Integer.valueOf(id_match);
            // Can we get the Owner object corresponding to this?
            metadata = new SimpleGdprMetadata(
                    getPurposesFromRequest(servlet),
                    protectionLevel,
                    getDataSubjectFromRequest(servlet, id),
                    new SimpleDataId(),
                    true,
                    true,
                    Identifiability.NotExplicit);
        }
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

        // TODO Check we are calling the right method

        // This might not work as we relocate the HttpServletRequest object...
        ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(parent);

        String name = null;
        if (parameters.length > 1) {
            IASString s = (IASString) parameters[0];
            if (s != null) {
                name = s.getString();
            }
        }

        // Debugging
        System.out.println("Servlet: " + request);
        //System.out.println("Stack Trace:");
        //Utils.printCurrentStackTrace();

        // Write the taint policy by hand
        IASString uri = request.getRequestURI();

        if (uri != null) {
            GdprMetadata metadata = null;

            // Check path
            String path = uri.getString();
            Matcher addnewPetsMatcher = addNewPetsPattern.matcher(path);
            Matcher editPetsMatcher = editPetsPattern.matcher(path);
            Matcher newVisitMatcher = addVisitPattern.matcher(path);

            if (path.equals("/owners/new")) {
                // New owner
                metadata = getMetadataFromOwnerRequest(request);
            } else if (path.matches("\\/owners\\/[0-9]+\\/edit")) {
                // Update owner
                metadata = getMetadataFromOwnerRequest(request);
            } else if (addnewPetsMatcher.matches()) {
                metadata = getMetadataFromPetRequest(request, addnewPetsMatcher, path, ProtectionLevel.Normal);
            } else if (editPetsMatcher.matches()) {
                metadata = getMetadataFromPetRequest(request, editPetsMatcher, path, ProtectionLevel.Normal);
            } else if (newVisitMatcher.matches()) {
                // The description should be higher sensitivity
                if ((name != null) && name.equals("description")) {
                    metadata = getMetadataFromPetRequest(request, newVisitMatcher, path, ProtectionLevel.Sensitive);
                } else {
                    metadata = getMetadataFromPetRequest(request, newVisitMatcher, path, ProtectionLevel.Normal);
                }
            }

            // Add taint information if match was found
            if (metadata != null) {
                System.out.println("Adding Taint metadata: " + metadata);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
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
