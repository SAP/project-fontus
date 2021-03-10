package com.sap.fontus.config;

import com.sap.fontus.Constants;
import com.sap.fontus.taintaware.bool.IASFactoryImpl;
import com.sap.fontus.taintaware.shared.IASFactory;

public enum TaintMethod {
    BOOLEAN(Constants.BOOLEAN_METHOD_NAME, Constants.BOOLEAN_METHOD_PATH, new IASFactoryImpl()),
    RANGE(Constants.RANGE_METHOD_NAME, Constants.RANGE_METHOD_PATH, new com.sap.fontus.taintaware.range.IASFactoryImpl()),
    LAZYCOMPLEX(Constants.LAZY_COMPLEX_METHOD_NAME, Constants.LAZY_COMPLEX_METHOD_PATH, new com.sap.fontus.taintaware.lazycomplex.IASFactoryImpl()),
    LAZYBASIC(Constants.LAZY_BASIC_METHOD_NAME, Constants.LAZY_BASIC_METHOD_PATH, new com.sap.fontus.taintaware.lazybasic.IASFactoryImpl()),
    ARRAY(Constants.ARRAY_METHOD_NAME, Constants.ARRAY_METHOD_PATH, new com.sap.fontus.taintaware.array.IASFactoryImpl()),
    UNTAINTED(Constants.UNTAINTED_METHOD_NAME, Constants.UNTAINTED_METHOD_PATH, new com.sap.fontus.taintaware.untainted.IASFactoryImpl());

    public static final String defaultTaintMethodName = Constants.BOOLEAN_METHOD_NAME;

    private final String path;
    private final String name;
    private final IASFactory factory;

    TaintMethod(String name, String path, IASFactory factory) {
        this.name = name;
        this.path = path;
        this.factory = factory;
    }

    public String getSubPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public IASFactory getFactory() {
        return this.factory;
    }

    public static TaintMethod getTaintMethodByArgumentName(String argName) {
        for (TaintMethod tm : TaintMethod.values()) {
            if (tm.name.equals(argName)) {
                return tm;
            }
        }
        throw new IllegalArgumentException("Taint method unknown:" + argName);
    }

    public static TaintMethod defaultTaintMethod() {
        return getTaintMethodByArgumentName(defaultTaintMethodName);
    }
}
