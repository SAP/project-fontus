package com.sap.fontus.gdpr.jforum2;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.Source;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.registry.RequiredPurposeRegistry;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.gdpr.metadata.simple.SimplePurposePolicy;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.gdpr.servlet.ReflectedSession;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

import java.util.*;

public class JForum2TaintHandler extends IASTaintHandler {

    private static RequiredPurposes getRequiredPurposesFromSink(String sinkFunction) {
        Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction);
        if(sink == null) return new RequiredPurposes.EmptyRequiredPurposes();

        return RequiredPurposeRegistry.getPurposeFromSink(sink);
    }
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId, String callerFunction) {
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



        DataSubject ds = new SimpleDataSubject(cookieId);
        Collection<AllowedPurpose> allowed = Utils.getPurposesFromRequest(request);
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

    public static IASTaintAware handleEmailTaint(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        if(taintAware != null && taintAware.isTainted() && taintAware.getTaintInformation() != null) {
            RequiredPurposes rp = getRequiredPurposesFromSink(sinkFunction);
            PurposePolicy policy = new SimplePurposePolicy();
            IASString tainted = taintAware.toIASString();
            for (IASTaintRange range : tainted.getTaintInformation().getTaintRanges(tainted.length())) {
                // Check policy for each range
                if (range.getMetadata() instanceof GdprTaintMetadata taintMetadata) {
                    GdprMetadata metadata = taintMetadata.getMetadata();
                    if (!policy.areRequiredPurposesAllowed(rp, metadata.getAllowedPurposes())) {
                        StringBuilder sb = new StringBuilder(50);
                        for(AllowedPurpose ap : metadata.getAllowedPurposes()) {
                            sb.append(ap);
                            sb.append(", ");
                        }
                        System.out.printf("Policy violation for %s!%nRequired: %s, got %s", tainted.getString(), rp, sb);
                        return null;
                    }
                }
            }
        }
        return taintAware;
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
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, JForum2TaintHandler::setTaint);

    }

    public static Object checkEmailTaint(Object object, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        if(callerFunction.equals("net/jforum/util/mail/Spammer.dispatchMessages()Z")) {
            return IASTaintHandler.checkTaint(object, instance, sinkFunction, sinkName, callerFunction, JForum2TaintHandler::handleEmailTaint);
        }
        return object;
    }


}
