package com.sap.fontus.sql.driver;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import com.sap.fontus.taintaware.unified.TaintInformationFactory;

import java.time.Instant;

public final class Utils {

    private Utils() {
    }

    public static String serializeTaints(IASString str) {
        Genson genson = new GensonBuilder()
                .withConverters(new Utils.InstantConverter())
                .useClassMetadata(true)
                .useRuntimeType(true)
                .useFields(true, VisibilityFilter.PRIVATE)
                .useMethods(false)
                .create();
        return genson.serialize(str.getTaintInformationInitialized().getTaintRanges(str.length()));
    }

    public static void restoreTaint(IASString str, String json) {
        IASTaintInformationable tis = parseTaint(json);
        str.setTaint(tis);
    }

    public static IASTaintInformationable parseTaint(String json) {
        Genson genson = new GensonBuilder()
                .withConverters(new Utils.InstantConverter())
                .useClassMetadata(true)
                .useRuntimeType(true)
                .useFields(true, VisibilityFilter.PRIVATE)
                .create();
        IASTaintRanges ranges = genson.deserialize(json, IASTaintRanges.class);
        return TaintInformationFactory.createTaintInformation(ranges.getLength(), ranges.getTaintRanges());
    }

    public static class InstantConverter implements Converter<Instant> {

        @Override
        public void serialize(Instant object, ObjectWriter writer, Context ctx) throws Exception {
            long epochSecond = object.getEpochSecond();
            int nano = object.getNano();
            writer.beginObject();
            writer.writeNumber("seconds", epochSecond);
            writer.writeNumber("nanos", nano);
            writer.endObject();
        }

        @Override
        public Instant deserialize(ObjectReader reader, Context ctx) throws Exception {
            reader.beginObject();
            long epochSecond = -1L;
            int nano = -1;
            while(reader.hasNext()) {
                reader.next();
                if("seconds".equals(reader.name())) {
                    epochSecond = reader.valueAsLong();
                } else if ("nanos".equals(reader.name())) {
                    nano = reader.valueAsInt();
                } else {
                    throw new IllegalStateException(String.format("Unknown field name: %s", reader.name()));
                }
            }
            reader.endObject();
            return Instant.ofEpochSecond(epochSecond, nano);
        }
    }
}
