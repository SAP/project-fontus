package com.sap.fontus.asm.resolver;

import com.sap.fontus.Constants;
import com.sap.fontus.utils.IOUtils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.ClassReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarClassResolver {
    public JarClassResolver() {
    }

    public Map<String, byte[]> loadClassesFrom(InputStream inputStream) {
        try(JarInputStream jis = new JarInputStream(inputStream)) {
            return this.loadClassesFrom(jis);
        } catch (IOException e) {
            throw new IllegalArgumentException("InputStream not readable as JAR");
        }
    }

    public Map<String, byte[]> loadClassesFrom(JarInputStream jarInputStream) {
        ConcurrentHashMap<String, byte[]> classData = new ConcurrentHashMap<>();

        this.findClassInJar(jarInputStream, classData);

        return classData;
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
}
