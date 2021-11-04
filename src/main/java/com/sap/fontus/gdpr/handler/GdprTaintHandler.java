package com.sap.fontus.gdpr.handler;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

public class GdprTaintHandler {

    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
        taintAware.setTaint(new IASBasicMetadata(source));
        System.out.println("FONTUS: Source: " + source.toString() + " taintAware: " + taintAware.toString());
        System.out.println("        Caller Type:" + parent.toString());
        System.out.println("        Input Parameters: " + parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            System.out.println("                  " + i + ": " + parameters[i].toString());
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
