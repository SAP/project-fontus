package com.sap.fontus.gdpr.sflight;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Source;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.broadleaf.BroadleafTaintHandler;
import com.sap.fontus.gdpr.cookie.ConsentCookie;
import com.sap.fontus.gdpr.cookie.ConsentCookieMetadata;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataId;
import com.sap.fontus.gdpr.metadata.simple.SimpleDataSubject;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.gdpr.sap.SapCloudTaintHandler;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class SflightTaintHandler extends IASTaintHandler {

    private static final IASString csrfTokenName = IASString.fromString("csrfToken");

    private static final String dataSubjectAttributeName = SflightTaintHandler.class.getName() + ".DATASUBJECT";

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
                System.out.println("Adding Taint metadata to string '" + taintAware.toString() + "': " + metadata);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
            }
        }
        return taintAware;
    }

    public static Object taint(Object object, Object parent, Object[] parameters, int sourceId, String callerFunction) {
        return IASTaintHandler.taint(object, parent, parameters, sourceId, callerFunction, SflightTaintHandler::setTaint);
    }

    public static Object checkPassenger(Object object, Object instance, String sinkFunction, String sinkName, String callerFunction) {
        // here magic to delete passenger in list without consent
        System.out.println("I'm here with: " + object.toString());
        return object;
    }
}
