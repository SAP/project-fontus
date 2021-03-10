package com.sap.fontus.utils.lookups;

import com.sap.fontus.utils.ClassUtils;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class JdkClassesLookup {
    private static final Logger logger = LogUtils.getLogger();

    private JdkClassesLookup(Collection<String> classes) {
        this.jdkClasses = new HashSet<>(classes.size());
        this.jdkClasses.addAll(classes);
        this.blacklistedPrefixes = new ArrayList<>();
        this.blacklistedPrefixes.addAll(Arrays.asList("sun/",
                "com/sun/proxy",
                "com/sun/crypto/",
                "jdk/",
                "java/",
                "sun/misc/",
                "net/sf/jopt-simple/",
                "org/objectweb/asm/",
                "org/openjdk/jmh/",
                "org/apache/commons/commons-math3/"));
    }

    public static JdkClassesLookup getInstance() {
        return JdkClassesLookup.LazyHolder.INSTANCE;
    }

    private static JdkClassesLookup initializeLookupTable(String inputFile) {
        try (InputStream inputStream = JdkClassesLookup.class
                .getClassLoader().getResourceAsStream(inputFile);
             InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)
        ) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            return new JdkClassesLookup(lines);

        } catch (Exception e) { //TODO: Think about proper error handling
            logger.error("Can't load the class list", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isJdkClass(String internalName, ClassLoader loader) {
        if (internalName == null) return true;

        boolean blacklisted = false;
        for (String blacklistedPrefix : blacklistedPrefixes) {
            if (internalName.startsWith(blacklistedPrefix)) {
                blacklisted = true;
                break;
            }
        }

        if (blacklisted && !internalName.startsWith("javax/servlet")) {
            return true;
        }

        // MXBeans have a reduced set of usable data types
        // Obviously IASString isn't part of it
        if (internalName.endsWith("MXBean") && ClassUtils.isInterface(internalName, loader)) {
            return true;
        }

        //TODO: is the split on $ the optimal way to only get the prefix? This is supposed to catch inner classes too
        String[] parts = internalName.split("\\$");
        assert parts[0] != null;
        return this.jdkClasses.contains(parts[0]);
    }

    private final Set<String> jdkClasses;

    private static class LazyHolder {
        private static final JdkClassesLookup INSTANCE = initializeLookupTable("openjdk12_classes.list"); // TODO: Make configurable
    }

    private final List<String> blacklistedPrefixes;

}
