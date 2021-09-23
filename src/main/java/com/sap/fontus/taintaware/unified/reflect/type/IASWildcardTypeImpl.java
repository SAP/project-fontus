package com.sap.fontus.taintaware.unified.reflect.type;

import com.sap.fontus.utils.ConversionUtils;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public class IASWildcardTypeImpl implements WildcardType {
    private final WildcardType type;
    public IASWildcardTypeImpl(WildcardType type) {
        this.type = type;
    }

    @Override
    public Type[] getUpperBounds() {
        return Arrays.stream(this.type.getUpperBounds()).map(ConversionUtils::convertTypeToInstrumented).toArray(Type[]::new);
    }

    @Override
    public Type[] getLowerBounds() {
        return Arrays.stream(this.type.getLowerBounds()).map(ConversionUtils::convertTypeToInstrumented).toArray(Type[]::new);
    }

    @Override
    public String getTypeName() {
        // TODO Instrument
        return this.type.getTypeName();
    }
}
