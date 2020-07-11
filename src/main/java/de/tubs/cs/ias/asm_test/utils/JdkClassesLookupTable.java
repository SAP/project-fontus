package de.tubs.cs.ias.asm_test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import de.tubs.cs.ias.asm_test.utils.Logger;

public final class JdkClassesLookupTable {
    private static final Logger logger = LogUtils.getLogger();

    private static int getJvmVersion() {
        String specVersion = System.getProperty("java.vm.specification.version");
        return Integer.parseInt(specVersion);
    }

    private static class LazyHolder {
        private static final JdkClassesLookupTable INSTANCE = initializeLookupTable("openjdk12_classes.list"); // TODO: Make configurable
    }

    public static JdkClassesLookupTable getInstance() {
        return JdkClassesLookupTable.LazyHolder.INSTANCE;
    }


    private static JdkClassesLookupTable initializeLookupTable(String inputFile) {
        try (InputStream inputStream = JdkClassesLookupTable.class
                .getClassLoader().getResourceAsStream(inputFile);
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)
        ) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            return new JdkClassesLookupTable(lines);

        } catch (IOException e) { //TODO: Think about proper error handling
            logger.error("Can't load the class list", e);
            return null;
        }
    }

    private final Set<String> jdkClasses;

    private JdkClassesLookupTable(Collection<String> classes) {
        this.jdkClasses = new HashSet<>(classes.size());
        this.jdkClasses.addAll(classes);
    }

    // TODO: Unify with Main
    private static final String[] blacklistedPrefixes = {
            "sun/",
            "com/sun/proxy",
            "jdk/",
            "java/",
            "sun/misc/",
            "net/sf/jopt-simple/",
            "org/objectweb/asm/",
            "org/openjdk/jmh/",
            "org/apache/commons/commons-math3"
    };

    public boolean isJdkClass(String className) {
        if (className == null) return true;

        boolean blacklisted = false;
        for(String blacklistedPrefix : blacklistedPrefixes) {
            if (className.startsWith(blacklistedPrefix)) {
                blacklisted = true;
                break;
            }
        }

        if (blacklisted && !className.startsWith("javax/servlet")) {
            return true;
        }
        //TODO: is the split on $ the optimal way to only get the prefix? This is supposed to catch inner classes too
        String[] parts = className.split("\\$");
        assert parts[0] != null;
        return this.jdkClasses.contains(parts[0]);
    }
}
