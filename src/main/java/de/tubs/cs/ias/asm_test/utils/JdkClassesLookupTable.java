package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.config.Configuration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class JdkClassesLookupTable {
    private static final ParentLogger logger = LogUtils.getLogger();

    public boolean isJdkClass(String className) {
        return JdkClassesLookupTable.getInstance().isJdkClass(className, null);
    }

    public boolean isJdkClass(String className, ClassLoader loader) {
        if (className == null) return true;

        boolean blacklisted = false;
        for (String blacklistedPrefix : blacklistedPrefixes) {
            if (className.startsWith(blacklistedPrefix)) {
                blacklisted = true;
                break;
            }
        }

        if (blacklisted && !className.startsWith("javax/servlet")) {
            return true;
        }

        // MXBeans have a reduced set of usable data types
        // Obviously IASString isn't part of it
        if (className.endsWith("MXBean") && ClassUtils.isInterface(className, loader)) {
            return true;
        }

        //TODO: is the split on $ the optimal way to only get the prefix? This is supposed to catch inner classes too
        String[] parts = className.split("\\$");
        assert parts[0] != null;
        return this.jdkClasses.contains(parts[0]);
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
            return new JdkClassesLookupTable(lines, Configuration.getConfiguration().getExcludedPackages());

        } catch (Exception e) { //TODO: Think about proper error handling
            logger.error("Can't load the class list", e);
            throw new RuntimeException(e);
        }
    }

    private final Set<String> jdkClasses;

    private JdkClassesLookupTable(Collection<String> classes, List<String> configuredBlacklistedPrefixes) {
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
        this.blacklistedPrefixes.addAll(configuredBlacklistedPrefixes);
    }

    private final List<String> blacklistedPrefixes;

    public boolean isJdkClass(Class<?> cls) {
        return isJdkClass(Utils.dotToSlash(cls.getName()));
    }
}
