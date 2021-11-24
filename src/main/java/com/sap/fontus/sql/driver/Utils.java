package com.sap.fontus.sql.driver;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import com.sap.fontus.taintaware.unified.TaintInformationFactory;

public class Utils {

    public static String serializeTaints(IASString str) {
        Genson genson = new GensonBuilder().useClassMetadata(true)
                .useRuntimeType(true).useFields(true, VisibilityFilter.PRIVATE).create();
        return genson.serialize(str.getTaintInformationInitialized().getTaintRanges(str.length()));
    }

    public static void restoreTaint(IASString str, String json) {
        Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true).useFields(true, VisibilityFilter.PRIVATE).create();
        IASTaintRanges ranges = genson.deserialize(json, IASTaintRanges.class);
        IASTaintInformationable tis = TaintInformationFactory.createTaintInformation(ranges.getLength(), ranges.getTaintRanges());
        str.setTaint(tis);
    }
}
