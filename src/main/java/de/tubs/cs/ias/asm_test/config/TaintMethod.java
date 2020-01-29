package de.tubs.cs.ias.asm_test.config;

public enum TaintMethod {
    BOOLEAN("bool/"), RANGE("range/");

    private final String path;

    TaintMethod(String path) {
        this.path = path;
    }

    public String getSubPath() {
        return this.path;
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
            case "bool":
                return TaintMethod.BOOLEAN;
            case "range":
                return TaintMethod.RANGE;
            default:
                throw new IllegalArgumentException("Taint method unknown:" + argName);
        }
    }

    public static TaintMethod defaultTaintMethod() {
        return BOOLEAN;
    }
}
