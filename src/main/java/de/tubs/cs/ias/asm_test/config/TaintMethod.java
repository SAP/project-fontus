package de.tubs.cs.ias.asm_test.config;

public enum TaintMethod {
    BOOLEAN("boolean", "bool/"), RANGE("range", "range/");

    public final static String defaultTaintMethodName = "boolean";
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
        return getSubPath().replace('/', '.');
    }

    public static TaintMethod getTaintMethodByPath(String path) {
        switch (path) {
            case "bool/":
                return TaintMethod.BOOLEAN;
            case "range/":
                return TaintMethod.RANGE;
            default:
                throw new IllegalArgumentException("Taint method/path unknown:" + path);
        }
    }

    public static TaintMethod getTaintMethodByArgumentName(String argName) {
        switch (argName) {
            case "boolean":
                return TaintMethod.BOOLEAN;
            case "range":
                return TaintMethod.RANGE;
            default:
                throw new IllegalArgumentException("Taint method unknown:" + argName);
        }
    }

    public static TaintMethod defaultTaintMethod() {
        return getTaintMethodByArgumentName(defaultTaintMethodName);
    }
}
