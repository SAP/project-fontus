package com.sap.fontus.gdpr;

import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleExpiryDate;
import com.sap.fontus.gdpr.petclinic.ConsentCookie;
import com.sap.fontus.gdpr.petclinic.ConsentCookieMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collection;

public final class Utils {
    private Utils() {}

    public static Object invokeGetter(Object obj, String name, int upWards) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> clazz = obj.getClass();
        for(int i = 0; i < upWards; i++) {
            clazz = clazz.getSuperclass();
        }
        Method m = clazz.getDeclaredMethod(name);
        m.setAccessible(true);
        return m.invoke(obj);
    }

    public static Object invokeGetter(Object obj, String name) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return invokeGetter(obj, name, 0);
    }
    public static boolean updateExpiryDatesAndProtectionLevel(IASTaintAware taintAware, long daysFromNow, ProtectionLevel protectionLevel) {
        if(!taintAware.isTainted()) {
            return false;
        }
        ExpiryDate expiryDate = new SimpleExpiryDate(Instant.now().plus(daysFromNow, ChronoUnit.DAYS));
        IASTaintInformationable taintInformation = taintAware.getTaintInformation();
        if(taintInformation == null) {
            return false;
        }
        IASTaintRanges ranges = taintInformation.getTaintRanges(-1);
        Collection<IASTaintRange> taintRanges = ranges.getTaintRanges();
        boolean adjusted = false;
        for(IASTaintRange range: taintRanges) {
            IASTaintMetadata meta = range.getMetadata();
            if(meta instanceof GdprTaintMetadata) {
                GdprTaintMetadata gdprTaintMetadata = (GdprTaintMetadata) meta;
                GdprMetadata gdprMetadata = gdprTaintMetadata.getMetadata();
                gdprMetadata.setProtectionLevel(protectionLevel);
                for(AllowedPurpose purpose : gdprMetadata.getAllowedPurposes()) {
                    purpose.setExpiryDate(expiryDate);
                    adjusted = true;
                }
            }
        }
        return adjusted;
    }


    public static Collection<AllowedPurpose> getPurposesFromRequest(ReflectedHttpServletRequest servlet) {
        ReflectedCookie[] cookies = servlet.getCookies();
        for (ReflectedCookie cookie : cookies) {
            if (ConsentCookie.isConsentCookie(cookie.getName().getString())) {
                System.out.println("Found Consent Cookie: " + cookie.getName().getString() + " = " + cookie.getValue().getString());
                ConsentCookie consentCookie = ConsentCookie.parse(cookie.getValue().getString());
                return ConsentCookieMetadata.getAllowedPurposesFromConsentCookie(consentCookie);
            }
        }
        // Return empty consent if no cookie is found
        return new ArrayList<>();
    }
}
