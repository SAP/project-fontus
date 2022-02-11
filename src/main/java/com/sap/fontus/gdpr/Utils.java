package com.sap.fontus.gdpr;

import com.sap.fontus.gdpr.metadata.AllowedPurpose;
import com.sap.fontus.gdpr.petclinic.ConsentCookie;
import com.sap.fontus.gdpr.petclinic.ConsentCookieMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public final class Utils {
    private Utils() {}

    public static Object invokeGetter(Object obj, String name) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method m = obj.getClass().getMethod(name);
        return m.invoke(obj);
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
