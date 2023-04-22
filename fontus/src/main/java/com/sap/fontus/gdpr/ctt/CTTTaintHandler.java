package com.sap.fontus.gdpr.ctt;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Source;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.gdpr.oh.OpenHospitalTaintHandler;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.gdpr.servlet.ReflectedSession;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.*;

public class CTTTaintHandler extends IASTaintHandler {

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

    private static final IASString CSRF_TOKEN = IASString.fromString("_csrf");
    private static final IASString ROOM_ID = IASString.fromString("roomId");
    private static final IASString ROOM_PIN = IASString.fromString("roomPin");


    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        if(parameters.length == 1 && (parameters[0].equals(CSRF_TOKEN) || parameters[0].equals(ROOM_ID) || parameters[0].equals(ROOM_PIN))) {
            return taintAware;
        }
        // TODO: ensure this is sufficient?
        if(!"org.springframework.security.web.context.HttpSessionSecurityContextRepository.SaveToSessionRequestWrapper".equals(parent.getClass().getCanonicalName())) {
            return taintAware;
        }
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);

        ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(parent);
        IASString uri = request.getRequestURI();
        if ("/r/checkIn".contentEquals(uri)) {
            // Checkin Handler

            return handleCheckinTaint(request, parameters, taintAware, sourceId);
        }
        String userId = getIdFromRequest(request);
        IASTaintMetadata metaData = getBasicTaintMetaDataFromRequest(request, userId, sourceId);
        taintAware.setTaint(metaData);

        return taintAware;
    }

    private static IASTaintAware handleCheckinTaint(ReflectedHttpServletRequest request, Object[] parameters, IASTaintAware taintAware, int sourceId) {
        String visitorEmail = request.getParameter("visitorEmail");
        IASTaintMetadata metaData = getBasicTaintMetaDataFromRequest(request, visitorEmail, sourceId);
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

    private static String getIdFromRequest(ReflectedHttpServletRequest request) {
        try {
            ReflectedSession session = request.getSession();
            Object securityContext = session.getAttribute(new IASString("SPRING_SECURITY_CONTEXT"));
            if(securityContext == null) {
                return null;
            }
            Method getAuthentication = securityContext.getClass().getMethod("getAuthentication");
            Object authentication = getAuthentication.invoke(securityContext);
            Method getPrincipal = authentication.getClass().getMethod("getPrincipal");
            //Method isAuthenticated = authentication.getClass().getMethod("isAuthenticated");
            Object principal = getPrincipal.invoke(authentication);
            //isAuthenticated.invoke(authentication);
            //Method getId = principal.getClass().getMethod("getId");
            Method getUsername = principal.getClass().getMethod("getUsername");
            //Method getAuthorities = principal.getClass().getMethod("getAuthorities");
            //String username = ((IASString) getUsername.invoke(principal)).getString();
            //getAuthorities.invoke(principal);
            IASString username =  (IASString) getUsername.invoke(principal);
            return username.getString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static IASTaintMetadata getBasicTaintMetaDataFromRequest(ReflectedHttpServletRequest request, String userId, int sourceId) {
        DataSubject ds = new SimpleDataSubject(userId);
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
        if (object instanceof IASTaintAware) {
            return setTaint((IASTaintAware) object, parent, parameters, sourceId);
        }
        return IASTaintHandler.traverseObject(object, taintAware -> setTaint(taintAware, parent, parameters, sourceId));
    }

    public static Object checkTaint(Object object, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        return checkTaint(object, instance, sinkFunction, sinkName, callerFunction, IASTaintHandler::handleTaint);
    }
}
