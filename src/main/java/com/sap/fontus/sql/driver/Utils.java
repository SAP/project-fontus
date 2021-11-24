package com.sap.fontus.sql.driver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.metadata.simple.SimpleGdprMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import com.sap.fontus.taintaware.unified.TaintInformationFactory;

import java.lang.reflect.Type;
import java.util.Set;

public class Utils {

    public static String serializeTaints(IASString str) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(str.getTaintInformationInitialized().getTaintRanges(str.length()));
        return json;
    }

    public static void restoreTaint(IASString str, String json) {
        Gson gson = new GsonBuilder().serializeNulls().registerTypeAdapter(IASTaintMetadata.class, new InstanceCreator<IASTaintMetadata>() {
            @Override
            public IASTaintMetadata createInstance(Type type) {
                return new GdprTaintMetadata(IASTaintSourceRegistry.TS_CS_UNKNOWN_ORIGIN, new SimpleGdprMetadata(Set.of(), ProtectionLevel.Undefined, null, null, false, false, Identifiability.Undefined));
            }
        }).registerTypeAdapter(GdprMetadata.class, new InstanceCreator<GdprMetadata>() {

            @Override
            public GdprMetadata createInstance(Type type) {
                return new SimpleGdprMetadata(Set.of(), ProtectionLevel.Undefined, null, null, false, false, Identifiability.Undefined);
            }
        }).create();

        IASTaintRanges ranges = gson.fromJson(json, IASTaintRanges.class);
        IASTaintInformationable tis = TaintInformationFactory.createTaintInformation(ranges.getLength(), ranges.getTaintRanges());
        str.setTaint(tis);
    }
}
