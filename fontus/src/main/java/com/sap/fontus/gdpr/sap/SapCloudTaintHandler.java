package com.sap.fontus.gdpr.sap;

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

public class SapCloudTaintHandler extends IASTaintHandler {

    private static final String dataSubjectAttributeName = SapCloudTaintHandler.class.getName() + ".DATASUBJECT";

    private static Method getMethod(Object o, String name) {
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    private static Collection<AllowedPurpose> getPurposesFromParameterInfo(Object parameterInfo) {
        try {
            Method m = getMethod(parameterInfo, "getHeader");
            if (m != null) {
                IASString s = (IASString) m.invoke(parameterInfo, new IASString(ConsentCookie.getConsentCookieName()));
                String header = s.getString();
                if (!header.isEmpty()) {
                    ConsentCookie consentCookie = ConsentCookie.parse(header);
                    return ConsentCookieMetadata.getAllowedPurposesFromConsentCookie(consentCookie);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return empty consent if no header is found
        return new ArrayList<>();
    }

    private static DataSubject getOrCreateDataSubjectUuid(String name) {
        return new SimpleDataSubject(name);
    }

    private static GdprMetadata createUserNameMetadata(String name, Object parameterInfo) {
        DataSubject dataSubject = getOrCreateDataSubjectUuid(name);
        return new SimpleGdprMetadata(getPurposesFromParameterInfo(parameterInfo), ProtectionLevel.Normal, dataSubject,
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
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        //Thread.dumpStack();
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        Source source = null;
        if (taintSource != null) {
            source = Configuration.getConfiguration().getSourceConfig().getSourceWithName(taintSource.getName());
            System.out.println("Source from Configuration: " + source);
        }

        // Check for ServletRequest getParameter function
        if ((parent != null) && (source != null)) {
            GdprMetadata metadata = createUserNameMetadata(taintAware.toIASString().getString(), parameters[0]);

            // Add taint information if match was found
            if (metadata != null) {
                System.out.println("Adding Taint metadata to string '" + taintAware + "': " + metadata);
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
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, SapCloudTaintHandler::setTaint);
    }

    public static Object checkTaint(Object object, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        return checkTaint(object, instance, sinkFunction, sinkName, callerFunction, IASTaintHandler::handleTaint);
    }

}
