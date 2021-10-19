package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.unified.IASInstrumenterInputStream;
import com.sap.fontus.taintaware.unified.IASString;

import java.io.InputStream;

public class IASClassLoaderProxy {
    public static InputStream getResourceAsStream(ClassLoader cls, IASString resource) {
        InputStream stream = cls.getResourceAsStream(resource.getString());
        if (Configuration.getConfiguration().isResourceToInstrument(resource.getString())) {
            return new IASInstrumenterInputStream(stream);
        }
        return stream;
    }
}
