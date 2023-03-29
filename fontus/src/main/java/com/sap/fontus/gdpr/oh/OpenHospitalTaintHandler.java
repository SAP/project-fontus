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
            Field f = inputStream.getClass().getField("in");
            f.setAccessible(true);

            Object pushBackInputStream = f.get(inputStream);
            Field f2 = pushBackInputStream.getClass().getField("in");
            f2.setAccessible(true);

            // org/eclipse/jetty/server/HttpInputOverHTTP
            Object httpInputOverHTTP = f2.get(pushBackInputStream);
            Field f3 = httpInputOverHTTP.getClass().getField("_channelState");
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
        if ((parent != null) && (source != null)) {

            GdprMetadata metadata = null;
            ReflectedHttpServletRequest request = getRequestFromStreamParser(parameters[0]);
            System.out.println("Request: " + request);

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
    public static Object taint(Object object, Object parent, Object[] parameters, int sourceId) {
        if (object instanceof IASTaintAware) {
            return setTaint((IASTaintAware) object, parent, parameters, sourceId);
        }
        return IASTaintHandler.traverseObject(object, taintAware -> setTaint(taintAware, parent, parameters, sourceId));
    }

}
