package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.utils.Logger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class TaintMethodConfig {
    private static final Logger logger = LogUtils.getLogger();

    private static final Properties properties;
    private static TaintMethod taintMethod;
    private static final String PATH = "taintpath";

    static {
        properties = new Properties();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream("application.properties");
        if (is != null) {
            try {
                properties.load(is);
            } catch (IOException e) {
                logger.error("Failed to load properties!", e);
            }
        } else {
            properties.setProperty(PATH, Constants.BOOLEAN_METHOD_PATH);
        }
    }

//    public static String getTaintSubPackage() {
//        return getTaintMethod().getSubPath();
//    }
//
//    public static TaintMethod getTaintMethod() {
//        if (taintMethod == null) {
//            String path = (String) properties.getOrDefault(PATH, "");
//            taintMethod = TaintMethod.getTaintMethodByPath(path);
//        }
//        return taintMethod;
//    }
}
