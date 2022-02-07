package com.sap.fontus.gdpr.openolat;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

public class OpenOlatTaintHandler extends IASTaintHandler {
    /**
     * Extracts the TCF consent string from a cookie and attaches it as the taint metadata
     * @param taintAware The Taint Aware String-like object
     * @param parent The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        //Source source = Configuration.getConfiguration().getSourceConfig().getSourceWithName(taintSource.getName());
        taintAware.setTaint(new IASBasicMetadata(taintSource));
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
         *     <owner>com/sap/fontus/gdpr/openolat/OpenOlatTaintHandler</owner>
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
