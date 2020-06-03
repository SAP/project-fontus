package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.Constants;

public enum TaintMethod {
    BOOLEAN(Constants.BOOLEAN_METHOD_NAME, Constants.BOOLEAN_METHOD_PATH),
    RANGE(Constants.RANGE_METHOD_NAME, Constants.RANGE_METHOD_PATH),
    LAZY_COMPLEX(Constants.LAZY_COMPLEX_METHOD_NAME, Constants.LAZY_COMPLEX_METHOD_PATH),
    ARRAY(Constants.ARRAY_METHOD_NAME, Constants.ARRAY_METHOD_PATH);

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
        for (TaintMethod tm : TaintMethod.values()) {
            if (tm.path.equals(path)) {
                return tm;
            }
        }
        throw new IllegalArgumentException("Taint method/path unknown:" + path);
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
