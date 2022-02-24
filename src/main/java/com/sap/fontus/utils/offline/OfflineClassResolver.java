package com.sap.fontus.utils.offline;

import com.sap.fontus.Constants;
import com.sap.fontus.agent.InstrumentationConfiguration;
import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.IClassResolver;
import com.sap.fontus.utils.IOUtils;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class OfflineClassResolver implements IClassResolver {
    private ClassResolver classResolver;
    private Map<String, byte[]> classes;

    public OfflineClassResolver() {
        this.classResolver = new ClassResolver(ClassLoader.getSystemClassLoader());
    }

    private synchronized void initialize() {
        if (this.isInitialized()) {
            return;
        }

        InstrumentationConfiguration configuration = InstrumentationConfiguration.getInstance();
        File input = configuration.getInput();

        ConcurrentHashMap<String, byte[]> classData = new ConcurrentHashMap<>();

        walkInput(input, classData);

        this.classes = Collections.unmodifiableMap(classData);
    }

    private boolean isInitialized() {
        return this.classes != null;
    }

    @Override
    public InputStream resolve(String className) {
        if (!isInitialized()) {
            this.initialize();
        }

        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }
        className = Utils.dotToSlash(className);

        InputStream inputStream = this.classResolver.resolve(className);
        if (inputStream != null) {
            return inputStream;
        }

        byte[] bytes = this.classes.get(className);

        if (bytes == null) {
            return null;
        }

        return new ByteArrayInputStream(bytes);
    }

    private void findClassInJarFile(File jar, Map<String, byte[]> classes) {
        try (JarInputStream jis = new JarInputStream(new FileInputStream(jar))) {

            this.findClassInJar(jis, classes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private void walkInput(File input, Map<String, byte[]> classes) {
        if (input.exists()) {
            if (input.isDirectory()) {
                File[] inputs = input.listFiles();
                if (inputs != null) {
                    for (File file : inputs) {
                        walkInput(file, classes);
                    }
                }
            } else {
                this.findClassInJarFile(input, classes);
            }
        }
    }
}
