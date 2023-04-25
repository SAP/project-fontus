package com.sap.fontus.gdpr.openolat;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Source;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.gdpr.openmrs.OpenMrsTaintHandler;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.gdpr.servlet.ReflectedSession;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OpenOlatTaintHandler extends IASTaintHandler {

    private static final Map<String, Collection<AllowedPurpose>> allowedPurposes = new HashMap<>();

    /**
     * Sets Taint Information in OpenOLAT according to request information.
     *
     * @param taintAware The Taint Aware String-like object
     * @param parent     The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId   The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setFormTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId, String callerName) {
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        assert (parameters.length == 4);
        try {
            Object ureq = parameters[2];
            Object sr = Utils.invokeGetter(ureq, "getHttpReq");
            ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(sr);
            //System.out.printf("Servlet Request: %s%n", request.toString());
            ReflectedSession rs = request.getSession();
            long userId = getSessionUserId(rs);
            String id = String.valueOf(userId);
            DataSubject ds = new SimpleDataSubject(id);
            Collection<AllowedPurpose> allowed = Utils.getPurposesFromRequest(request);
            allowedPurposes.put(id, allowed);
            GdprMetadata metadata = new SimpleGdprMetadata(
                    allowed,
                    ProtectionLevel.Normal,
                    ds,
                    new SimpleDataId(),
                    true,
                    true,
                    Identifiability.NotExplicit);
            taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return taintAware;
    }

    public static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId, String callerName) {
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        IASTaintMetadata metaData = getBasicTaintMetaDataFromRequest(parent, sourceId);
        taintAware.setTaint(metaData);
        return taintAware;
    }

    private static IASTaintMetadata getBasicTaintMetaDataFromRequest(Object requestObject, int sourceId) {
        ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(requestObject);
        ReflectedSession session = request.getSession();
        long sessionId = getSessionUserId(session);
        String userId = String.valueOf(sessionId);
        if(sessionId == -1L) {
            // if userId == -1 -> not logged in -> give marker value that is hopefully more "special"
            userId = "FONTUS_CHANGE_ME";
        }

        DataSubject ds = new SimpleDataSubject(userId);
        Collection<AllowedPurpose> allowed = Utils.getPurposesFromRequest(request);
        allowedPurposes.put(userId, allowed);
        GdprMetadata metadata = new SimpleGdprMetadata(
                allowed,
                ProtectionLevel.Normal,
                ds,
                new SimpleDataId(),
                true,
                true,
                Identifiability.NotExplicit);
        return new GdprTaintMetadata(sourceId, metadata);
    }

    /**
     * The taint method can be used as a taintHandler for a given taint source
     *
     * @param object   The object to be tainted
     * @param sourceId The ID of the taint source function
     * @return The tainted object
     * <p>
     * This snippet of XML can be added to the source:
     *
     * <pre>
     * {@code
     * <tainthandler>
     * <opcode>184</opcode>
     * <owner>com/sap/fontus/gdpr/openolat/OpenOlatTaintHandler</owner>
     * <name>taint</name>
     * <descriptor>(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;I)Ljava/lang/Object;</descriptor>
     * <interface>false</interface>
     * </tainthandler>
     * }
     * </pre>
     */
    public static Object taint(Object object, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, OpenOlatTaintHandler::setTaint);
    }

    public static Object formTaint(Object object, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, OpenOlatTaintHandler::setFormTaint);

    }

    public static Object contactTracingTaint(Object object, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, OpenOlatTaintHandler::setContactTracingTaint);

    }
    public static IASTaintAware setContactTracingTaint(Object object, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        IASTaintAware taintAware = (IASTaintAware) object;

        try {
            if (!taintAware.isTainted()) {
                System.err.printf("The string '%s' should be tainted but it isn't!!!!%n", taintAware);
                Object identity = Utils.invokeGetter(parent, "getIdentity", 2);
                long userId = (long) Utils.invokeGetter(identity, "getKey");
                String id = String.valueOf(userId);
                DataSubject ds = new SimpleDataSubject(id);
                Collection<AllowedPurpose> allowed = allowedPurposes.getOrDefault(id, new ArrayList<>());

                GdprMetadata metadata = new SimpleGdprMetadata(
                        allowed,
                        ProtectionLevel.Normal,
                        ds,
                        new SimpleDataId(),
                        true,
                        true,
                        Identifiability.NotExplicit);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
            }
            // TODO: Adjust expiry date accordingly
            boolean adjusted = Utils.updateExpiryDatesAndProtectionLevel(taintAware, 14L, ProtectionLevel.Sensitive);
            System.out.printf("Adjusted the expiry date/protection level for String '%s' successfully: %b%n", taintAware, adjusted);
            return taintAware;
        } catch(Exception ex) {
            com.sap.fontus.utils.Utils.logException(ex);
            return taintAware;

        }
    }

    private static Long getSessionUserId(ReflectedSession session) {
        try {
            Object us = session.getAttribute(new IASString("org.olat.core.util.UserSession"));
            Object si = Utils.invokeGetter(us, "getSessionInfo");
            if (si == null) {
                return -1L;
            }
            return (Long) Utils.invokeGetter(si, "getIdentityKey");
        } catch (Exception ex) {
            //ex.printStackTrace();
            return -1L;
        }
    }
}
