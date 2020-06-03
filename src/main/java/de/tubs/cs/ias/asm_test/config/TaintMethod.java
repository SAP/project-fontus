package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.Constants;

public enum TaintMethod {
    BOOLEAN(Constants.BOOLEAN_METHOD_NAME, Constants.BOOLEAN_METHOD_PATH), RANGE(Constants.RANGE_METHOD_NAME, Constants.RANGE_METHOD_PATH), LAZY_COMPLEX(Constants.LAZY_COMPLEX_METHOD_NAME, Constants.LAZY_COMPLEX_METHOD_PATH);

    public static final String defaultTaintMethodName = Constants.BOOLEAN_METHOD_NAME;

    private final String path;
    private final String name;

    TaintMethod(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getSubPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public String getSubPackage() {
        return this.path.replace('/', '.');
    }

    public static TaintMethod getTaintMethodByPath(String path) {
        switch (path) {
            case Constants.BOOLEAN_METHOD_PATH:
                return BOOLEAN;
            case Constants.RANGE_METHOD_PATH:
                return RANGE;
            case Constants.LAZY_COMPLEX_METHOD_PATH:
                return LAZY_COMPLEX;
            default:
                throw new IllegalArgumentException("Taint method/path unknown:" + path);
        }
    }

    public static TaintMethod getTaintMethodByArgumentName(String argName) {
        switch (argName) {
            case Constants.BOOLEAN_METHOD_NAME:
                return BOOLEAN;
            case Constants.RANGE_METHOD_NAME:
                return RANGE;
            case Constants.LAZY_COMPLEX_METHOD_NAME:
                return LAZY_COMPLEX;
            default:
                throw new IllegalArgumentException("Taint method unknown:" + argName);
        }
    }

    public static TaintMethod defaultTaintMethod() {
        return getTaintMethodByArgumentName(defaultTaintMethodName);
    }
}
