package com.sap.fontus.taintaware.unified;

import com.sap.fontus.utils.ConversionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class IASObjectInputStream extends ObjectInputStream {
    public IASObjectInputStream(InputStream in) throws IOException {
        super(in);
        super.enableResolveObject(true);
    }

    public static ObjectInputStream fromStream(InputStream is) throws IOException {
        //System.out.println("Creating IASObjectInputStream!");
        return new IASObjectInputStream(is);
    }

    protected IASObjectInputStream() throws IOException {
        super();
        super.enableResolveObject(true);
    }

    @Override
    protected Object resolveObject(Object obj) throws IOException {
        Object converted = ConversionUtils.convertToInstrumented(obj);
        //System.out.printf("Converted: %s -> %s%n", obj.getClass().getName(), converted.getClass().getName());
        return converted;
    }
}
