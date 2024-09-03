package com.sap.fontus.gdpr.openmrs;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;

import com.sap.fontus.config.Sink;
import com.sap.fontus.config.Source;

import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.registry.PurposeRegistry;
import com.sap.fontus.gdpr.metadata.registry.VendorRegistry;
import com.sap.fontus.gdpr.metadata.simple.*;
import com.sap.fontus.gdpr.cookie.ConsentCookie;
import com.sap.fontus.gdpr.cookie.ConsentCookieMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class OpenMrsTaintHandler extends IASTaintHandler {

    private static final String dataSubjectAttributeName = OpenMrsTaintHandler.class.getName() + ".DATASUBJECT";

    private static final String appIdParameterName = "appId";
    private static final String registerPatientApp = "referenceapplication.registrationapp.registerPatient";
    private static final String patientIdParameterName = "patientId";
    private static final String personIdParameterName = "personId";

    private static final FunctionCall getParameterFunctionCall = new FunctionCall(
            Opcodes.INVOKEINTERFACE,
            "javax/servlet/ServletRequest",
            "getParameter",
            "(Ljava/lang/String;)Ljava/lang/String;",
            true);

    private static final FunctionCall getHttpParameterFunctionCall = new FunctionCall(
            Opcodes.INVOKEINTERFACE,
            "javax/servlet/http/HttpServletRequest",
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

    private static final Set<FunctionCall> allowedFunctionCalls = new HashSet<>(Arrays.asList(getParameterFunctionCall, getHttpParameterFunctionCall, getParameterValuesFunctionCall, getHttpParameterValuesFunctionCall, getParameterMapFunctionCall));

    private static Collection<AllowedPurpose> getPurposesFromRequest(ReflectedHttpServletRequest servlet) {
        ReflectedCookie[] cookies = servlet.getCookies();
        for (ReflectedCookie cookie : cookies) {
            if (ConsentCookie.isConsentCookie(cookie.getName().getString())) {
                //System.out.println("Found Consent Cookie: " + cookie.getName().getString() + " = " + cookie.getValue().getString());
                ConsentCookie consentCookie = ConsentCookie.parse(cookie.getValue().getString());
                return ConsentCookieMetadata.getAllowedPurposesFromConsentCookie(consentCookie);
            }
        }
        // Return empty consent if no cookie is found
        return new ArrayList<>();
    }

    public static Integer getInteger(String strNum) {
        Integer i = null;
        if (strNum == null) {
            return i;
        }
        try {
            i = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return i;
        }
        return i;
    }

    private static GdprMetadata getTaintMetadataFromExistingUuid(ReflectedHttpServletRequest request, String patientId) {
        GdprMetadata md = null;
        if (patientId == null) {
            return null;
        }
        try {
            // Spring stores the application context in the HttpRequest attributes
            // Should be a org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext
            Object obj = request.getAttribute(new IASString("org.springframework.web.servlet.DispatcherServlet.CONTEXT"));

            // According to applicationContext-service.xml from OpenMRS (https://github.com/openmrs/openmrs-core/blob/master/api/src/main/resources/applicationContext-service.xml)
            //
            // 	<bean id="serviceContext" class="org.openmrs.api.context.ServiceContext" factory-method="getInstance"
            //		  destroy-method="destroyInstance">
            //		<property name="patientService" ref="patientService"/>
            //		<property name="personService" ref="personService"/>
            //      ...
            //	</bean>

            // Through this context we can access each of the Spring Beans
            Method m = obj.getClass().getMethod("getBean", IASString.class);
            // OpenMRS has a bean called patientService, with class PatientService
            Object bean = m.invoke(obj, new IASString("patientService"));

            // Some OpenMRS pages use a UUID to retrieve patientId, some use the database ID (ie an integer)
            // Try to check here which method to choose
            Object patient = null;
            Integer i = getInteger(patientId);
            if (i != null) {
                // This interface will be proxied by Fontus, but the method names are the same
                Method m2 = bean.getClass().getMethod("getPatient", Integer.class);
                patient = m2.invoke(bean, i);
            } else {
                // This interface will be proxied by Fontus, but the method names are the same
                Method m2 = bean.getClass().getMethod("getPatientByUuid", IASString.class);
                patient = m2.invoke(bean, new IASString(patientId));
            }

            // Get some data which should have some metadata attached when it was created
            Method getUuid = patient.getClass().getMethod("getGender");
            IASString extracted = (IASString) getUuid.invoke(patient);

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

        } catch (Exception e) {
            System.err.println("Exception trying to extract taint metadata: " + e.getMessage());
        }
        //System.out.println("FONTUS: for person UUID: " + patientId + " found taint metadata: " + md);
        return md;
    }

    private static GdprMetadata getMetaDataFromRequest(ReflectedHttpServletRequest request) {
        String patientId = request.getParameter(patientIdParameterName);
        if (patientId == null) {
            // Sometimes the patientId stored in a personId parameter...
            patientId = request.getParameter(personIdParameterName);
        }
        return getTaintMetadataFromExistingUuid(request, patientId);
    }

    private static DataSubject getOrCreateDataSubjectUuid(ReflectedHttpServletRequest request) {
        DataSubject dataSubject = null;
        // First try retrieving from cached attribute value
        Object o = request.getAttribute(dataSubjectAttributeName);
        if ((o instanceof DataSubject)) {
            dataSubject = (DataSubject) o;
        } else {
            dataSubject = new SimpleDataSubject(UUID.randomUUID().toString());
            request.setAttribute(dataSubjectAttributeName, dataSubject);
        }
        //System.out.println("FONTUS: got data subject uuid: " + dataSubject);
        return dataSubject;
    }

    private static GdprMetadata createNewPatientMetadata(ReflectedHttpServletRequest request) {
        DataSubject dataSubject = getOrCreateDataSubjectUuid(request);
        return new SimpleGdprMetadata(getPurposesFromRequest(request), ProtectionLevel.Normal, dataSubject,
                new SimpleDataId(), true, true, Identifiability.Explicit);
    }

    /**
     * Sets Taint Information in OpenMrs according to request information.
     * @param taintAware The Taint Aware String-like object
     * @param parent The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId, String CallerFunction) {
        // General debug info
        //IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        Source source = null;
        if (taintSource != null) {
            source = Configuration.getConfiguration().getSourceConfig().getSourceWithName(taintSource.getName());
            //System.out.println("Source from Configuration: " + source);
        }

        // Check for ServletRequest getParameter function
        if ((parent != null) && (source != null) && allowedFunctionCalls.contains(source.getFunction())) {

            GdprMetadata metadata = null;

            ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(parent);
            // System.out.println("Request: " + request);
            if (registerPatientApp.equals(request.getParameter(appIdParameterName))) {
                //System.out.println("Creating new patient...");
                metadata = createNewPatientMetadata(request);
            } else {
                //System.out.println("Not creating patient...");
                metadata = getMetaDataFromRequest(request);
            }

            // Add taint information if match was found
            if (metadata != null) {
                //System.out.println("Adding Taint metadata to string '" + taintAware.toString() + "': " + metadata);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
            }
        }
        return taintAware;
    }

    private static IASTaintAware setDiagnosisTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId, String CallerFunction) {
        // Instrument JsonNode.get("diagnosis").getTextValue(), ie
        // Source will be: org/codehaus/jackson/JsonNode/getTextValue()Ljava/lang/String;
        // parent = JsonNode
        // Make this a specific source, so that it is only called inside
        // i.e. org/openmrs/module/coreapps/htmlformentry/EncounterDiagnosesElement.applyAction(Lorg/openmrs/module/htmlformentry/FormEntrySession;)V
        // parameters = nominally zero, but add locals #0 (ie a String)

        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);

        // Check parameter length
        if (parameters.length != 1) {
            System.out.printf("Parameter length %d != 1%n", parameters.length);
            return taintAware;
        }
        Object listObject = parameters[0];
        if (!(listObject instanceof IASString)) {
            System.out.printf("Parameter class %s != %s", listObject.getClass().getName(), IASString.class.getName());
            return taintAware;
        }
        // This should already be tainted correctly
        IASString jsonList = (IASString) listObject;
        // Simplest is to just propagate taint to the output
        // Might lead to over-tainting, but should be fine in this specific case.
        if (jsonList.isTainted()) {
            IASTaintMetadata metadata = jsonList.getTaintInformation().getTaint(0);
            // As a diagnosis is sensitive information, set the appropriate bit:
            if (metadata instanceof GdprTaintMetadata gdprMetadata) {
                gdprMetadata.getMetadata().setProtectionLevel(ProtectionLevel.Sensitive);
            } else {
                System.err.println("Metadata is not of type GdprTaintMetadata! Actual type: " + metadata.getClass());
            }
            //System.out.println("Adding Taint metadata from string '" + jsonList + "' to string '" + taintAware.toString() + "': " + metadata);
            taintAware.setTaint(metadata);
        }
        return taintAware;
    }

    /**
     * The taint method can be used as a taintHandler for a given taint source
     * @param object The object to be tainted
     * @param sourceId The ID of the taint source function
     * @return The tainted object
     * <p>
     * This snippet of XML can be added to the source:
     *
     * <pre>
     * {@code
     * <tainthandler>
     *     <opcode>184</opcode>
     *     <owner>com/sap/fontus/gdpr/openolat/OpenOlatTaintHandler</owner>
     *     <name>taint</name>
     *     <descriptor>(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;I)Ljava/lang/Object;</descriptor>
     *     <interface>false</interface>
     * </tainthandler>
     * }
     * </pre>
     */
    public static Object taint(Object object, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, OpenMrsTaintHandler::setTaint);
    }

    public static Object diagnosisTaint(Object object, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, OpenMrsTaintHandler::setDiagnosisTaint);
    }

    public static IASTaintAware applyPolicy(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, RequiredPurposes requiredPurposes) {

        Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction);

        // Create a policy
        PurposePolicy policy = new SimplePurposePolicy();

        // Extract taint information
        IASString taintedString = taintAware.toIASString();
        if (taintedString.isTainted() && (taintedString.getTaintInformation() != null)) {
            boolean policyViolation = false;
            for (IASTaintRange range : taintedString.getTaintInformation().getTaintRanges(taintedString.getString().length())) {
                // Check policy for each range
                if (range.getMetadata() instanceof GdprTaintMetadata taintMetadata) {
                    GdprMetadata metadata = taintMetadata.getMetadata();
                    if (!policy.areRequiredPurposesAllowed(requiredPurposes, metadata.getAllowedPurposes())) {
                        policyViolation = true;
                    }
                }
            }
            // Block / Sanitize / etc...
            if (policyViolation) {
                Abort a = sink.getAbortFromSink();
                taintAware = a.abort(taintAware, instance, sinkFunction, sinkName, Arrays.asList(Thread.currentThread().getStackTrace()));
            }
        }
        return taintAware;
    }

    // Assume the purpose can be inferred by looking at which methods are calling the sink in question
    public static RequiredPurposes getRequiredPurposesFromStackTrace(StackTraceElement[] stack) {
        Set<Purpose> purposes = new HashSet<>();
        Set<Vendor> vendors = new HashSet<>();
        for (StackTraceElement ste : stack) {
            String clazzName = ste.getClassName();
            if (clazzName.startsWith("org.openmrs.module.fhir2")) {
                Purpose purpose = PurposeRegistry.getInstance().get("export");
                purposes.add(purpose);
                Vendor vendor = VendorRegistry.getInstance().get("fhir");
                vendors.add(vendor);
                //System.out.println("FONTUS: Mapped class " + clazzName + " to purpose: " + purpose + " vendor: " + vendor);
            }
        }
        return new SimpleRequiredPurposes(purposes, vendors);
    }

    /**
     * This is called for sink functions
     */
    public static IASTaintAware handleTaint(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        boolean isTainted = taintAware.isTainted();
        // System.out.println("isTainted : " + isTainted);
        // System.out.println("taintaware : " + taintAware);
        // System.out.println("sink : " + sinkFunction);
        // System.out.println("stackTrace : " + java.util.Arrays.toString(Thread.currentThread().getStackTrace()));

        if (isTainted) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            // General idea: get the "Purpose" by navigating the stack trace to find who is calling the sink function
            return applyPolicy(taintAware, instance, sinkFunction, sinkName, getRequiredPurposesFromStackTrace(stackTrace));
        }
        return taintAware;
    }

    public static Object checkTaint(Object object, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        if ("org.hl7.fhir.r4.model.HumanName".equals(object.getClass().getName())) {
            try {
                Method m = object.getClass().getMethod("getNameAsSingleString");
                IASString s = (IASString) m.invoke(object);
                // Actually should iterate over all names and replace them
                IASTaintHandler.checkTaint(s, instance, sinkFunction, sinkName, callerFunction, OpenMrsTaintHandler::handleTaint);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                System.err.println("Exception: " + e + " for object: " + object);
            }
            return object;
        }
        return IASTaintHandler.checkTaint(object, instance, sinkFunction, sinkName, callerFunction, OpenMrsTaintHandler::handleTaint);
    }

    // Assume the purpose can be inferred by looking at which methods are calling the sink in question
    public static RequiredPurposes getRequiredPurposesFromLoggedInUser() {
        // Get user-based purpose from the logged in user: org.openmrs.api.context.Context.getAuthenticatedUser

        String loggedInUser = "UnknownUser";

        try {
            Class<?> contextClass = Class.forName("org.openmrs.api.context.Context", false, Thread.currentThread().getContextClassLoader());
            Object user = contextClass.getMethod("getAuthenticatedUser").invoke(null);
            if (user != null) {
                Object userName = user.getClass().getMethod("getUsername").invoke(user);
                // Will be an IASString because it is tainted...
                if (userName instanceof IASString) {
                    loggedInUser = ((IASString) userName).getString();
                }
            }
        } catch (Exception e) {
            System.err.println("Exception getting logged in user: " + e);
        }
        //System.out.println("FONTUS: logged in user: " + loggedInUser);
        Set<Purpose> purposes = new HashSet<>();
        Set<Vendor> vendors = new HashSet<>();
        purposes.add(PurposeRegistry.getInstance().get("processing"));
        // Cut suffix for bencmark
        if(loggedInUser.startsWith("doctor")) {
            loggedInUser = "doctor";
        } else if(loggedInUser.startsWith("clerk")) {
            loggedInUser = "clerk";
        }
        vendors.add(VendorRegistry.getInstance().get(loggedInUser));
        return new SimpleRequiredPurposes(purposes, vendors);
    }

    public static IASTaintAware handleLoggedInUserTaint(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        boolean isTainted = taintAware.isTainted();

        // System.out.println("isTainted : " + isTainted);
        // System.out.println("taintaware : " + taintAware);
        // System.out.println("sink : " + sinkFunction);
        // System.out.println("stackTrace : " + java.util.Arrays.toString(Thread.currentThread().getStackTrace()));

        if (isTainted) {
            return applyPolicy(taintAware, instance, sinkFunction, sinkName, getRequiredPurposesFromLoggedInUser());
        }
        return taintAware;
    }

    public static Object checkLoggedInUserTaint(Object object, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        return IASTaintHandler.checkTaint(object, instance, sinkFunction, sinkName, callerFunction, OpenMrsTaintHandler::handleLoggedInUserTaint);
    }
}
