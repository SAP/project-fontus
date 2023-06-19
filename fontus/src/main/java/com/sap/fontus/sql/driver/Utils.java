package com.sap.fontus.sql.driver;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import com.sap.fontus.taintaware.unified.TaintInformationFactory;
import com.sap.fontus.utils.InstantConverter;

public final class Utils {
    private static final Genson serializer = new GensonBuilder()
                .withConverters(new InstantConverter())
            .useClassMetadata(true)
                .useRuntimeType(true)
                .useFields(true, VisibilityFilter.PRIVATE)
                .useMethods(false)
                .create();
    private static final Genson deserializer =new GensonBuilder()
            .withConverters(new InstantConverter())
            .useClassMetadata(true)
            .useRuntimeType(true)
            .useFields(true, VisibilityFilter.PRIVATE)
            .create();
    private Utils() {
    }

    public static String serializeTaints(IASString str) {

        return serializer.serialize(str.getTaintInformationInitialized().getTaintRanges(str.length()));
    }

    public static void restoreTaint(IASString str, String json) {
        IASTaintInformationable tis = parseTaint(json);
        str.setTaint(tis);
    }

    public static IASTaintInformationable parseTaint(String json) {

        IASTaintRanges ranges = deserializer.deserialize(json, IASTaintRanges.class);
        return ranges != null ? TaintInformationFactory.createTaintInformation(ranges.getLength(), ranges.getTaintRanges()) : null;
    }

}
