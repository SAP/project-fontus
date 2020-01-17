package de.tubs.cs.ias.asm_test.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TaintMethodConfig {
    private static final Properties properties;
    private static String taintPath;

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
        if(taintPath == null) {
            taintPath = (String) properties.getOrDefault("taintpath", "");
        }
        return taintPath;
    }
}
