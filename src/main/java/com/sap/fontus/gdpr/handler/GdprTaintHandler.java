package com.sap.fontus.gdpr.handler;

import com.iabtcf.decoder.TCString;
import com.sap.fontus.gdpr.metadata.GdprMetadata;
import com.sap.fontus.gdpr.tcf.TcfBackedGdprMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

public class GdprTaintHandler extends IASTaintHandler {

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
        ReflectedHttpServletRequest servlet = new ReflectedHttpServletRequest(parent);
        System.out.println("URL: " + servlet.getRequestURL());

        ReflectedCookie[] cookies = servlet.getCookies();
        System.out.println("Cookies: " + cookies);
        if (cookies != null) {
            for (ReflectedCookie cookie : cookies) {
                System.out.println(cookie);
            }
        }

        String euconsent_name = "euconsent";
        String euconsent_v2_name = "euconsent_v2";
        TCString vendorConsent = null;
        for (ReflectedCookie cookie : cookies) {
            // Make sure v2 is given priority
            if (cookie.getName().equals(euconsent_v2_name)) {
                vendorConsent = TCString.decode(cookie.getValue().getString());
                break;
            } else if (cookie.getName().equals(euconsent_name)) {
                vendorConsent = TCString.decode(cookie.getValue().getString());
                break;
            }
        }
        if (vendorConsent != null) {
            System.out.println("TCF Cookie: " + vendorConsent.toString());
            GdprMetadata metadata = new TcfBackedGdprMetadata(vendorConsent);
            System.out.println("Metadata: " + metadata);
        } else {
            System.out.println("No euconsent Cookie found, try this one: BOEFEAyOEFEAyAHABDENAI4AAAB9vABAASA");
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
