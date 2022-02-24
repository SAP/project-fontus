package com.sap.fontus;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.IOUtils;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class OfflineJarInstrumenter {
    private static final int OneKB = 1024;
    private static final Logger logger = LogUtils.getLogger();

    private final List<String> classes = new ArrayList<>();
    private final Configuration configuration;
    private final OfflineClassInstrumenter offlineClassInstrumenter;

    public OfflineJarInstrumenter(Configuration configuration) {
        this.configuration = configuration;
        this.offlineClassInstrumenter = new OfflineClassInstrumenter(configuration);
    }

    public List<String> getClasses() {
        return classes;
    }

    private void instrumentJarFile(JarInputStream jis, JarOutputStream jos) throws IOException {
        for (JarEntry jei = jis.getNextJarEntry(); jei != null; jei = jis.getNextJarEntry()) {
            JarEntry jeo = new JarEntry(jei.getName());

            logger.info("Reading jar entry: {}", jei.getName());

            jos.putNextEntry(jeo);

            if (!jei.isDirectory()) {
                byte[] entryBytes = IOUtils.readStream(jis);
                InputStream jeis = new ByteArrayInputStream(entryBytes);

                CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup(Thread.currentThread().getContextClassLoader());

                if (jei.getName().endsWith(Constants.CLASS_FILE_SUFFIX) &&
                        !combinedExcludedLookup.isJdkClass(jei.getName()) &&
                        !combinedExcludedLookup.isFontusClass(jei.getName()) &&
                        !combinedExcludedLookup.isExcluded(jei.getName())
                ) {
                    try {
                        this.offlineClassInstrumenter.instrumentClassStream(jeis, jos);
                        this.classes.add(Utils.replaceClassFileSuffix(jei.getName()));
                    } catch (Exception e) {
                        logger.error("Class %s could not be instrumented: %s", jei.getName(), e);
                    }
                } else if (jei.getName().endsWith(Constants.JAR_FILE_SUFFIX)) {
                    ByteArrayOutputStream innerJarBos = new ByteArrayOutputStream();
                    JarOutputStream innerJos = new JarOutputStream(innerJarBos);
                    JarInputStream innerJis = new JarInputStream(jeis);

                    this.instrumentJarFile(innerJis, innerJos);

                    copySingleEntry(new ByteArrayInputStream(innerJarBos.toByteArray()), jos);

                    innerJos.close();
                } else {
                    copySingleEntry(jeis, jos);
                }
                jeis.close();
            }
            jos.closeEntry();
        }
    }

    public void instrumentJarFile(File input, File output) throws IOException {
        JarOutputStream jos;
        try (JarInputStream jis = new JarInputStream(new FileInputStream(input))) {
            FileOutputStream fos = new FileOutputStream(output);
            jos = new JarOutputStream(fos);

            logger.info("Reading jar file from: {}", input.getAbsolutePath());

            // MANIFEST.MD sometimes not outputted
            // Quickfix
            JarFile in = new JarFile(input);
            JarEntry manifestIn = in.getJarEntry("META-INF/MANIFEST.MF");
            if (manifestIn != null) {
                InputStream manifestInputStream = in.getInputStream(manifestIn);
                jos.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
                copySingleEntry(manifestInputStream, jos);
                jos.closeEntry();
            }

            this.instrumentJarFile(jis, jos);
        }

        jos.close();

        logger.info("Writing transformed jar file to: {}", output.getAbsolutePath());
    }

    private static void copySingleEntry(InputStream i, OutputStream o) throws IOException {
        int len = 0;
        byte[] buffer = new byte[OneKB];

        while ((len = i.read(buffer, 0, buffer.length)) != -1) {
            o.write(buffer, 0, len);
        }
    }
}
