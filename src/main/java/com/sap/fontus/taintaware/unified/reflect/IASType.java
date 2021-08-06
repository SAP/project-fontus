package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASString;

import java.lang.reflect.Type;

public class IASType implements Type {
    private static final InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
    private final Type type;

    public IASType(Type type) {
        this.type = type;
    }

    @Override
    public String getTypeName() {
        return instrumentationHelper.translateClassName(type.getTypeName()).orElse(type.getTypeName());
    }
}
