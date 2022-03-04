package com.sap.fontus.asm.resolver;

import com.sap.fontus.Constants;
import com.sap.fontus.utils.IOUtils;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarClassResolver implements IClassResolver {
    private volatile boolean isInitializing;
    private final JarInputStream jarInputStream;
    private Map<String, byte[]> classes;

    JarClassResolver(InputStream inputStream) throws IOException {
        this.jarInputStream = new JarInputStream(inputStream);
    }

    synchronized void initialize() {
        if (this.isInitializing || this.isInitialized()) {
            return;
        }

        this.isInitializing = true;

        ConcurrentHashMap<String, byte[]> classData = new ConcurrentHashMap<>();

        this.findClassInJar(this.jarInputStream, classData);

        this.classes = Collections.unmodifiableMap(classData);

        this.isInitializing = false;
    }

    private boolean isInitialized() {
        return this.classes != null;
    }

    @Override
    public InputStream resolve(String className) {
        if (!isInitialized()) {
            throw new IllegalStateException("JarClassResolver not initialized yet");
        }

        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }
        className = Utils.dotToSlash(className);

        byte[] bytes = this.classes.get(className);

        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }

        return null;
    }


    public Map<String, byte[]> getClasses() {
        if (!this.isInitialized()) {
            throw new IllegalStateException("JarClassResolver not initialized yet");
        }
        return new HashMap<>(classes);
    }

//    private void findClassInJarFile(File jar, Map<String, byte[]> classes) {
//        try (JarInputStream jis = new JarInputStream(new FileInputStream(jar))) {
//
//            this.findClassInJar(jis, classes);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void findClassInJar(JarInputStream jis, Map<String, byte[]> classes) {
        try {
            for (JarEntry jei = jis.getNextJarEntry(); jei != null; jei = jis.getNextJarEntry()) {
                if (!jei.isDirectory()) {
                    byte[] entryBytes = IOUtils.readStream(jis);
                    InputStream jeis = new ByteArrayInputStream(entryBytes);

                    CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup(Thread.currentThread().getContextClassLoader());

                    if (jei.getName().endsWith(Constants.CLASS_FILE_SUFFIX) &&
                            !combinedExcludedLookup.isJdkClass(jei.getName()) &&
                            !combinedExcludedLookup.isFontusClass(jei.getName()) &&
                            !combinedExcludedLookup.isExcluded(jei.getName())
                    ) {
                        String name = new ClassReader(entryBytes).getClassName();
                        classes.putIfAbsent(name, entryBytes);
                    } else if (jei.getName().endsWith(Constants.JAR_FILE_SUFFIX)) {
                        JarInputStream innerJis = new JarInputStream(jeis);

                        this.findClassInJar(innerJis, classes);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
