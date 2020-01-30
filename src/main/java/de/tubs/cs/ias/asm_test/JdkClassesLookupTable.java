package de.tubs.cs.ias.asm_test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.*;

public final class JdkClassesLookupTable {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final JdkClassesLookupTable instance = initializeLookupTable("openjdk12_classes.list"); // TODO: Make configurable

    private static int getJvmVersion() {
        String specVersion = System.getProperty("java.vm.specification.version");
        return Integer.parseInt(specVersion);
    }


    private static JdkClassesLookupTable initializeLookupTable(String inputFile) {
        try (InputStream inputStream = JdkClassesLookupTable.class
                .getClassLoader().getResourceAsStream(inputFile);
             InputStreamReader isr = new InputStreamReader(inputStream, "UTF8");
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

    public boolean isJdkClass(String className) {
        if (className == null) return true;
        if ((className.startsWith("sun/") || className.startsWith("com/sun/proxy") || className.startsWith("jdk/") || className.startsWith("java/") || className.startsWith("sun/misc/") || className.startsWith("org/objectweb/asm/") || className.startsWith("org/openjdk/jmh/") || className.startsWith("org/apache/commons/commons-math3") || className.startsWith("net/sf/jopt-simple/")) && !className.startsWith("javax/servlet")) {
            return true;
        }
        //TODO: is the split on $ the optimal way to only get the prefix? This is supposed to catch inner classes too
        String[] parts = className.split("\\$");
        assert parts[0] != null;
        return this.jdkClasses.contains(parts[0]);
    }
}
