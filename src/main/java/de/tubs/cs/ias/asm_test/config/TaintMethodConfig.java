package de.tubs.cs.ias.asm_test.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TaintMethodConfig {
    private static final Properties properties;
    private static TaintMethod taintMethod;

    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream("application.properties");
        properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTaintSubPackage() {
        return getTaintMethod().getPath();
    }

    public static TaintMethod getTaintMethod() {
        if(taintMethod == null) {
            String path = (String) properties.getOrDefault("taintpath", "");
            taintMethod = TaintMethod.getTaintMethodByPath(path);
        }
        return taintMethod;
    }

    public enum TaintMethod {
        BOOLEAN("bool/"), RANGE("range/");

        private final String path;

        TaintMethod(String path) {
            this.path = path;
        }

        public String getPath() {
            return this.path;
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
    }
}
