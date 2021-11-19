package com.sap.fontus.gdpr.tcf;

import com.iabtcf.decoder.TCString;
import com.sap.fontus.gdpr.metadata.GdprMetadata;
import com.sap.fontus.gdpr.metadata.GdprTaintMetadata;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

public class TcfTaintHandler extends IASTaintHandler {

    private static String consent_name = "euconsent";
    private static String consent_v2_name = "euconsent_v2";

    /**
     * Extracts the TCF consent string from a cookie and attaches it as the taint metadata
     * @param taintAware The Taint Aware String-like object
     * @param parent The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {

        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);

        // This might not work as we relocate the HttpServletRequest object...
        ReflectedHttpServletRequest servlet = new ReflectedHttpServletRequest(parent);

        System.out.print(servlet.toString());

        ReflectedCookie[] cookies = servlet.getCookies();
        TCString vendorConsent = null;
        for (ReflectedCookie cookie : cookies) {
            // Make sure v2 is given priority
            if (cookie.getName().equals(consent_v2_name)) {
                vendorConsent = TCString.decode(cookie.getValue().getString());
                break;
            } else if (cookie.getName().equals(consent_name)) {
                vendorConsent = TCString.decode(cookie.getValue().getString());
                break;
            }
        }
        if (vendorConsent != null) {
            System.out.println("TCF Cookie: " + vendorConsent.toString());
            GdprMetadata metadata = new TcfBackedGdprMetadata(vendorConsent);
            System.out.println("Metadata: " + metadata.toString());
            taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
        } else {
            System.out.println("No euconsent[_v2] Cookie found!");
        }
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
