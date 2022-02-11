package com.sap.fontus.gdpr.openmrs;

import com.mysql.cj.x.protobuf.MysqlxCursor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Source;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.gdpr.petclinic.ConsentCookie;
import com.sap.fontus.gdpr.petclinic.ConsentCookieMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.objectweb.asm.Opcodes;

import javax.xml.crypto.Data;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class OpenMrsTaintHandler extends IASTaintHandler {

    private static final String dataSubjectAttributeName = OpenMrsTaintHandler.class.getName() + ".DATASUBJECT";

    private static final String appIdParameterName = "appId";
    private static final String registerPatientApp = "referenceapplication.registrationapp.registerPatient";
    private static final String patientId = "patientId";

    private static final FunctionCall getParameterFunctionCall = new FunctionCall(
            Opcodes.INVOKEINTERFACE,
            "javax/servlet/ServletRequest",
            "getParameter",
            "(Ljava/lang/String;)Ljava/lang/String;",
            true);

    private static final FunctionCall getParameterValuesFunctionCall = new FunctionCall(
            Opcodes.INVOKEINTERFACE,
            "javax/servlet/ServletRequest",
            "getParameterValues",
            "(Ljava/lang/String;)[Ljava/lang/String;",
            true);

    private static final FunctionCall getHttpParameterValuesFunctionCall = new FunctionCall(
            Opcodes.INVOKEINTERFACE,
            "javax/servlet/http/HttpServletRequest",
            "getParameterValues",
            "(Ljava/lang/String;)[Ljava/lang/String;",
            true);
    
    private static final FunctionCall getParameterMapFunctionCall = new FunctionCall(
            Opcodes.INVOKEINTERFACE,
            "javax/servlet/ServletRequest",
            "getParameterMap",
            "()Ljava/util/Map;",
            true);

    private static Collection<AllowedPurpose> getPurposesFromRequest(ReflectedHttpServletRequest servlet) {
        ReflectedCookie[] cookies = servlet.getCookies();
        for (ReflectedCookie cookie : cookies) {
            if (ConsentCookie.isConsentCookie(cookie.getName().getString())) {
                System.out.println("Found Consent Cookie: " + cookie.getName().getString() + " = " + cookie.getValue().getString());
                ConsentCookie consentCookie = ConsentCookie.parse(cookie.getValue().getString());
                return ConsentCookieMetadata.getAllowedPurposesFromConsentCookie(consentCookie);
            }
        }
        // Return empty consent if no cookie is found
        return new ArrayList<>();
    }

    private static GdprMetadata getTaintMetadataFromExistingUuid(String uuid) {
        GdprMetadata md = null;
	if (uuid == null) {
	    return null;
	}
        try {
            // OpenMRS has a global context for retrieving objects from the DB:
            Class<?> clazz = Class.forName("org.openmrs.api.context.Context");
            // Through this context we can access the person service
            Method getPersonService = clazz.getMethod("getPersonService");
            Object personService = getPersonService.invoke(null);
            // Now extract the person object from the DB
            Method getPersonByUuid = personService.getClass().getMethod("getPersonByUuid", IASString.class);
            Object person = getPersonByUuid.invoke(personService, new IASString(uuid));

            // Get some data which should have some metadata attached when it was created
            Method getUuid = person.getClass().getMethod("getGender");
            IASString extracted = (IASString) getUuid.invoke(person);

            // With any luck, this UUID will contain the original taint information
            IASTaintRanges ranges = extracted.getTaintInformation().getTaintRanges(extracted.length());
            for (IASTaintRange range : ranges) {
                IASTaintMetadata metadata = range.getMetadata();
                if (metadata instanceof GdprTaintMetadata) {
                    md = ((GdprTaintMetadata) metadata).getMetadata();
                    // Take metadata from first tainted region
                    break;
                }
            }

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            System.err.println("Exception trying to extract taint metadata: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("FONTUS: for person UUID: " + uuid + " found taint metadata: " + md);
        return md;
    }

    private static DataSubject getDataSubjectFromUuid(String uuid) {
	GdprMetadata md = getTaintMetadataFromExistingUuid(uuid);
	if (md != null) {
	    return new SimpleDataSubject(getTaintMetadataFromExistingUuid(uuid).getSubject());
	}
	return null;
    }

    private static DataSubject getDataSubjectFromRequestParameter(ReflectedHttpServletRequest request) {
        return getDataSubjectFromUuid(request.getParameter(patientId));
    }

    private static DataSubject getOrCreateDataSubjectUuid(ReflectedHttpServletRequest request) {
        DataSubject dataSubject = null;
        Object o = request.getAttribute(dataSubjectAttributeName);
        if ((o != null) && (o instanceof DataSubject)) {
            dataSubject = (DataSubject) o;
        } else {
            dataSubject = new SimpleDataSubject(UUID.randomUUID().toString());
            request.setAttribute(dataSubjectAttributeName, dataSubject);
        }
	System.out.println("FONTUS: got data subject uuid: " + dataSubject);
        return dataSubject;
    }

    private static GdprMetadata createNewPatientMetadata(ReflectedHttpServletRequest request) {
        DataSubject dataSubject = getOrCreateDataSubjectUuid(request);
        return new SimpleGdprMetadata(getPurposesFromRequest(request), ProtectionLevel.Normal, dataSubject,
                new SimpleDataId(), true, true, Identifiability.Explicit);
    }

    private static GdprMetadata getPatientMetadata(ReflectedHttpServletRequest request) {
        DataSubject dataSubject = getDataSubjectFromRequestParameter(request);
	if (dataSubject != null) {
	    return new SimpleGdprMetadata(getPurposesFromRequest(request), ProtectionLevel.Normal, dataSubject,
					  new SimpleDataId(), true, true, Identifiability.Explicit);
	}
	return null;
    }

    /**
     * Sets Taint Information in OpenMrs according to request information.
     * @param taintAware The Taint Aware String-like object
     * @param parent The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        Source source = null;
	if (taintSource != null) {
	    source = Configuration.getConfiguration().getSourceConfig().getSourceWithName(taintSource.getName());
	    System.out.println("Source from Configuration: " + source);
	}

        // Check for ServletRequest getParameter function
        if ((parent != null) && (source != null) &&
                (source.getFunction().equals(getParameterFunctionCall) ||
                 source.getFunction().equals(getParameterValuesFunctionCall) ||
		 source.getFunction().equals(getHttpParameterValuesFunctionCall) ||
                 source.getFunction().equals(getParameterMapFunctionCall))) {

            GdprMetadata metadata = null;

            ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(parent);
	    System.out.println("Request: " + request);
            if (registerPatientApp.equals(request.getParameter(appIdParameterName))) {
		System.out.println("Creating new patient...");
                metadata = createNewPatientMetadata(request);
            } else {
		System.out.println("Not creating patient...");
                metadata = getPatientMetadata(request);
            }

            // Add taint information if match was found
            if (metadata != null) {
                System.out.println("Adding Taint metadata to string '" + taintAware.toString() + "': " + metadata);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
            } else {
		System.out.println("Null metadata, not tainting!");
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
         *     <owner>com/sap/fontus/gdpr/openolat/OpenOlatTaintHandler</owner>
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
