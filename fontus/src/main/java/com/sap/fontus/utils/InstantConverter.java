package com.sap.fontus.utils;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import java.time.Instant;

public class InstantConverter implements Converter<Instant> {

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
        while (reader.hasNext()) {
            reader.next();
            if ("seconds".equals(reader.name())) {
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
