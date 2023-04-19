package com.sap.fontus.gdpr.oh;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.Source;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.gdpr.cookie.ConsentCookie;
import com.sap.fontus.gdpr.cookie.ConsentCookieMetadata;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.registry.PurposeRegistry;
import com.sap.fontus.gdpr.metadata.registry.VendorRegistry;
import com.sap.fontus.gdpr.metadata.simple.*;
import com.sap.fontus.gdpr.openmrs.OpenMrsTaintHandler;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class OpenHospitalTaintHandler extends IASTaintHandler {

    private static final String dataSubjectAttributeName = OpenHospitalTaintHandler.class.getName() + ".DATASUBJECT";

    private static Collection<AllowedPurpose> getPurposesFromRequest(ReflectedHttpServletRequest servlet) {
        String header = servlet.getHeader(ConsentCookie.getConsentCookieName());
        if ((header != null) && !header.isEmpty()) {
            ConsentCookie consentCookie = ConsentCookie.parse(header);
            return ConsentCookieMetadata.getAllowedPurposesFromConsentCookie(consentCookie);
        }
        // Return empty consent if no header is found
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

    private static DataSubject getOrCreateDataSubjectUuid(ReflectedHttpServletRequest request) {
        DataSubject dataSubject;
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

    private static ReflectedHttpServletRequest getRequestFromStreamParser(Object parser) {
        ReflectedHttpServletRequest request = null;

        try {
            // DefaultDeserializationContext -> getParser()
            // com.fasterxml.jackson.core.UTF8StreamJsonParser -> getInputSource()
            // org.springframework.util.StreamUtils$NonClosingInputStream -> in
            // org/eclipse/jetty/server/HttpInputOverHTTP
            // org/eclipse/jetty/server/HttpChannelState

            Method m = parser.getClass().getMethod("getInputSource");

            Object inputStream = m.invoke(parser);
            Field f = inputStream.getClass().getSuperclass().getDeclaredField("in");
            f.setAccessible(true);

            Object pushBackInputStream = f.get(inputStream);
            Field f2 = pushBackInputStream.getClass().getSuperclass().getDeclaredField("in");
            f2.setAccessible(true);

            // org/eclipse/jetty/server/HttpInputOverHTTP
            Object httpInputOverHTTP = f2.get(pushBackInputStream);
            Field f3 = httpInputOverHTTP.getClass().getSuperclass().getDeclaredField("_channelState");
            f3.setAccessible(true);


            Object channelState = f3.get(httpInputOverHTTP);
            Method m2 = channelState.getClass().getMethod("getBaseRequest");
            Object requestObject = m2.invoke(channelState);

            request = new ReflectedHttpServletRequest(requestObject);

        } catch (Exception e) {
            System.err.println("Exception trying to extract request: " + e.getMessage());
        }
        return request;
    }


    /**
     * Sets Taint Information in OpenMrs according to request information.
     * @param taintAware The Taint Aware String-like object
     * @param parent The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        Source source = null;
        if (taintSource != null) {
            source = Configuration.getConfiguration().getSourceConfig().getSourceWithName(taintSource.getName());
            System.out.println("Source from Configuration: " + source);
        }

        // Check for ServletRequest getParameter function
        if ((parent != null) && (source != null)) {

            GdprMetadata metadata = null;
            ReflectedHttpServletRequest request = getRequestFromStreamParser(parameters[0]);
            System.out.println("Request: " + request);

            if (request.getMethodString().equals("POST") && request.getRequestURIString().endsWith("patients")) {
                System.out.println("Creating new patient...");
                metadata = createNewPatientMetadata(request);
            }
            // Add taint information if match was found
            if (metadata != null) {
                System.out.println("Adding Taint metadata to string '" + taintAware.toString() + "': " + metadata);
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
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, OpenHospitalTaintHandler::setTaint);
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
                if (range.getMetadata() instanceof GdprTaintMetadata) {
                    GdprTaintMetadata taintMetadata = (GdprTaintMetadata) range.getMetadata();
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
    public static RequiredPurposes getRequiredPurposesFromLoggedInUser() {
        // Get user-based purpose from the logged in user: org.openmrs.api.context.Context.getAuthenticatedUser

        String loggedInUser = "UnknownUser";

        try {
            // Spring framework call to get user name:
            // org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName()
            Class<?> contextClass = Class.forName("org.springframework.security.core.context.SecurityContextHolder", false, Thread.currentThread().getContextClassLoader());
            Object context = contextClass.getMethod("getContext").invoke(null);

            if (context != null) {
                Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
                if (authentication != null) {
                    Object userName = authentication.getClass().getMethod("getName").invoke(authentication);
                    // Will be an IASString because it is tainted, or maybe not!
                    if (userName instanceof IASString) {
                        loggedInUser = ((IASString) userName).getString();
                    } else if (userName instanceof String) {
                        loggedInUser = (String) userName;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception getting logged in user: " + e);
        }
        //System.out.println("FONTUS: logged in user: " + loggedInUser);
        Set<Purpose> purposes = new HashSet<>();
        Set<Vendor> vendors = new HashSet<>();
        purposes.add(PurposeRegistry.getInstance().get("processing"));
        vendors.add(VendorRegistry.getInstance().get(loggedInUser));
        return new SimpleRequiredPurposes(purposes, vendors);
    }

    public static IASTaintAware handleLoggedInUserTaint(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        boolean isTainted = taintAware.isTainted();

        if (isTainted) {
            return applyPolicy(taintAware, instance, sinkFunction, sinkName, getRequiredPurposesFromLoggedInUser());
        }
        return taintAware;
    }

    public static Object checkTaint(Object object, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        return checkTaint(object, instance, sinkFunction, sinkName, callerFunction, OpenHospitalTaintHandler::handleLoggedInUserTaint);
    }

}
