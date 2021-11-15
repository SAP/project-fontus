package com.sap.fontus.gdpr.handler;

import com.iab.gdpr.Purpose;
import com.iab.gdpr.consent.VendorConsent;
import com.iab.gdpr.consent.VendorConsentDecoder;
import com.iab.gdpr.consent.VendorConsentEncoder;
import com.iab.gdpr.consent.implementation.v1.VendorConsentBuilder;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServlet;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GdprTaintHandler {

    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);

        System.out.println("FONTUS: Source: " + source);
        System.out.println("        taintAware: " + taintAware);
        System.out.println("        Caller Type:" + parent);
        System.out.println("        Input Parameters: " + parameters);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                System.out.println("                  " + i + ": " + parameters[i].toString());
            }
        }

        // This might not work as we relocate the HttpServletRequest object...
        ReflectedHttpServlet servlet = new ReflectedHttpServlet(parent);
        System.out.println("URL: " + servlet.getRequestURL());

        ReflectedCookie[] cookies = servlet.getCookies();
        System.out.println("Cookies: " + cookies);
        if (cookies != null) {
            for (ReflectedCookie cookie : cookies) {
                System.out.println(cookie);
            }
        }

        String euconsent_name = "euconsent";
        VendorConsent vendorConsent = null;
        for (ReflectedCookie cookie : cookies) {
            if (cookie.getName().equals(euconsent_name)) {
                vendorConsent = VendorConsentDecoder.fromBase64String(cookie.getValue().getString());
            }
        }
        if (vendorConsent != null) {
            System.out.println("TCF Cookie: " + vendorConsent.toString());
        } else {
            final VendorConsent vc = new VendorConsentBuilder()
                    .withConsentRecordCreatedOn(Instant.now())
                    .withConsentRecordLastUpdatedOn(Instant.now())
                    .withCmpID(5)
                    .withCmpVersion(1)
                    .withConsentScreenID(1)
                    .withConsentLanguage("en")
                    .withVendorListVersion(1)
                    .withAllowedPurposes(Stream.of(Purpose.AD_SELECTION, Purpose.STORAGE_AND_ACCESS, Purpose.PERSONALIZATION)
                            .collect(Collectors.toCollection(HashSet::new)))
                    .withMaxVendorId(10)
                    .withVendorEncodingType(1)
                    .withDefaultConsent(false)
                    .build();
            final String base64String = VendorConsentEncoder.toBase64String(vendorConsent);
            System.out.println("No euconsent Cookie found, try this one: " + base64String);
            // BOEFEAyOEFEAyAHABDENAI4AAAB9vABAASA
        }
        //taintAware.setTaint(new IASBasicMetadata(source));
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
     *     <owner>com/sap/fontus/gdpr/GdprTaintHandler/handler</owner>
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
}
