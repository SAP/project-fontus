package com.sap.fontus.gdpr.handler;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASTaintHandler;

public class GdprTaintHandler {

    private static IASTaintAware setTaint(IASTaintAware taintAware, int sourceId) {
        IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
        taintAware.setTaint(new IASBasicMetadata(source));
        return taintAware;
    }

    public static Object taint(Object object, int sourceId) {
        if (object instanceof IASTaintAware) {
            IASTaintSource source = IASTaintSourceRegistry.getInstance().get(sourceId);
            ((IASTaintAware) object).setTaint(new IASBasicMetadata(source));
            return object;
        }
        return IASTaintHandler.traverseObject(object, taintAware -> setTaint(taintAware, sourceId));
    }
}
