package com.sap.fontus.gdpr.openolat;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Source;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.gdpr.servlet.ReflectedSession;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

public class OpenOlatTaintHandler extends IASTaintHandler {

    /**
     * Sets Taint Information in OpenOLAT according to request information.
     * @param taintAware The Taint Aware String-like object
     * @param parent The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setFormTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        assert (parameters.length == 4);
            try {
                Object ureq = parameters[2];
                Object sr = Utils.invokeGetter(ureq, "getHttpReq");
                ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(sr);
                //System.out.printf("Servlet Request: %s%n", request.toString());
                ReflectedSession rs = request.getSession();
                long userId = getSessionUserId(rs);
                DataSubject ds = new SimpleDataSubject(String.valueOf(userId));
                GdprMetadata metadata = new SimpleGdprMetadata(
                        Utils.getPurposesFromRequest(request),
                        ProtectionLevel.Normal,
                        ds,
                        new SimpleDataId(),
                        true,
                        true,
                        Identifiability.NotExplicit);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            return taintAware;
    }
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        Source source = Configuration.getConfiguration().getSourceConfig().getSourceWithName(taintSource.getName());
        taintAware.setTaint(new IASBasicMetadata(taintSource));
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

    public static Object formTaint(Object object, Object parent, Object[] parameters, int sourceId) {
        if (object instanceof IASTaintAware) {
            return setFormTaint((IASTaintAware) object, parent, parameters, sourceId);
        }
        return IASTaintHandler.traverseObject(object, taintAware -> setFormTaint(taintAware, parent, parameters, sourceId));
    }

    public static Object contactTracingTaint(Object object, Object parent, Object[] parameters, int sourceId) {
        IASTaintAware taintAware = (IASTaintAware) object;
        // Adjust Taint Metadata to add expiry data
        return object;
    }

    private static Long getSessionUserId(ReflectedSession session) {
        try {
            Object us = session.getAttribute(new IASString("org.olat.core.util.UserSession"));
            Object si = Utils.invokeGetter(us, "getSessionInfo");
            Long identityKey = (Long) Utils.invokeGetter(si, "getIdentityKey");
            return identityKey;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1L;
        }
    }
}
