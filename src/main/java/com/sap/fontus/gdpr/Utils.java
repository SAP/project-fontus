package com.sap.fontus.gdpr;

import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleExpiryDate;
import com.sap.fontus.gdpr.cookie.ConsentCookie;
import com.sap.fontus.gdpr.cookie.ConsentCookieMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.BiFunction;

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

    // Works like a fold in OCaml/Haskell or accumulate in Python
    // Applies the function to each GdprMetadata in the taintInformation and accumulates information
    public static <A> A processGdprMetaData(IASTaintInformationable taintInformation, A initial,  BiFunction<A, GdprMetadata, A> function) {
        A accumulator = initial;
        if(taintInformation == null) {
            return accumulator;
        }
        IASTaintRanges ranges = taintInformation.getTaintRanges(-1);
        Collection<IASTaintRange> taintRanges = ranges.getTaintRanges();

        for(IASTaintRange range: taintRanges) {
            IASTaintMetadata meta = range.getMetadata();
            if(meta instanceof GdprTaintMetadata) {
                GdprTaintMetadata gdprTaintMetadata = (GdprTaintMetadata) meta;
                GdprMetadata gdprMetadata = gdprTaintMetadata.getMetadata();
                accumulator = function.apply(accumulator, gdprMetadata);
            }
        }
        return accumulator;
    }

    public static Collection<DataSubject> getDataSubjects(IASTaintInformationable taintInformation) {
        Collection<DataSubject> dataSubjects = new HashSet<>();
        return processGdprMetaData(taintInformation, dataSubjects, (acc, gdprData) -> {
            acc.add(gdprData.getSubject());
            return acc;
        });
    }

    public static boolean isDataExpired(IASTaintInformationable taintInformation, Instant now) {
        return processGdprMetaData(taintInformation, false, (acc, gdprData) -> {
            for(AllowedPurpose purpose : gdprData.getAllowedPurposes()) {
                ExpiryDate expiryDate = purpose.getExpiryDate();
                if(expiryDate.hasExpiry() && expiryDate.getDate().isBefore(now)) {
                    return true;
                }
            }
            return acc;
        });
    }

    public static boolean updateExpiryDatesAndProtectionLevel(IASTaintAware taintAware, long daysFromNow, ProtectionLevel protectionLevel) {
        if(!taintAware.isTainted()) {
            return false;
        }
        IASTaintInformationable taintInformation = taintAware.getTaintInformation();
        if(taintInformation == null) {
            return false;
        }
        ExpiryDate expiryDate = new SimpleExpiryDate(Instant.now().plus(daysFromNow, ChronoUnit.DAYS));
        return processGdprMetaData(taintInformation, false, (acc, gdprData) -> {
            gdprData.setProtectionLevel(protectionLevel);
            for(AllowedPurpose purpose : gdprData.getAllowedPurposes()) {
                purpose.setExpiryDate(expiryDate);
                acc = true;
            }
            return acc;
        });
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

    public static IASTaintAware censorContestedParts(IASTaintAware taintAware) {
        if (taintAware.isTainted()) {
            IASString s = taintAware.toIASString();
            if (s != null) {
                StringBuilder sb = new StringBuilder(s.getString());
                for (IASTaintRange range : s.getTaintInformation().getTaintRanges(s.length())) {
                    IASTaintMetadata meta = range.getMetadata();
                    if(meta instanceof GdprTaintMetadata) {
                        GdprMetadata gdprMetadata = ((GdprTaintMetadata) meta).getMetadata();
                        if(!gdprMetadata.isProcessingUnrestricted()) {
                            for (int i = range.getStart(); i < range.getEnd(); i++) {
                                sb.setCharAt(i, '*');
                            }
                        }
                    }
                }
                taintAware = taintAware.newInstance();
                taintAware.setContent(sb.toString(), s.getTaintInformationCopied());
            }
        }
        return taintAware;
    }
}
