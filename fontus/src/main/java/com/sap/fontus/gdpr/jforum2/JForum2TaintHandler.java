package com.sap.fontus.gdpr.jforum2;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Source;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
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

public class JForum2TaintHandler extends IASTaintHandler {

    private static final Map<String, Collection<AllowedPurpose>> allowedPurposes = new HashMap<>();


    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        Source source = Configuration.getConfiguration().getSourceConfig().getSourceWithName(taintSource.getName());
        IASTaintMetadata metaData = getBasicTaintMetaDataFromRequest(parent, sourceId);
        taintAware.setTaint(metaData);
        return taintAware;
    }

// JForumExecutionContext.getRequest().getSessionContext().getId()
    private static String getCookieId(ReflectedHttpServletRequest request) {
        ReflectedCookie[] cookies = request.getCookies();
        for(ReflectedCookie cookie : cookies) {
            if("jforumUserId".equals(cookie.getName().getString())) {
                return cookie.getValue().getString();
            }
        }
        return null;
    }
    private static IASTaintMetadata getBasicTaintMetaDataFromRequest(Object requestObject, int sourceId) {
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(requestObject);
        ReflectedSession session = request.getSession();
        String cookieId = getCookieId(request);
        // Alternative: SessionFacade.getUserSession().getUserId()
        long sessionId = -1L;
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
    public static Object taint(Object object, Object parent, Object[] parameters, int sourceId) {
        if (object instanceof IASTaintAware) {
            return setTaint((IASTaintAware) object, parent, parameters, sourceId);
        }
        return IASTaintHandler.traverseObject(object, taintAware -> setTaint(taintAware, parent, parameters, sourceId));
    }
}
