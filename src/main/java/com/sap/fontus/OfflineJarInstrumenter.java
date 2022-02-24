package com.sap.fontus;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.IOUtils;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.apache.commons.codec.digest.DigestUtils;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

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

    private void instrumentJarFile(JarInputStream jis, JarOutputStream jos, boolean root) throws IOException {
        for (JarEntry jei = jis.getNextJarEntry(); jei != null; jei = jis.getNextJarEntry()) {
            logger.info("Reading jar entry: {}", jei.getName());

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
                        byte[] bytes = this.offlineClassInstrumenter.instrumentClassStream(jeis);

                        JarEntry jeo = createJarEntry(jei.getName(), bytes);
                        jos.putNextEntry(jeo);
                        jos.write(bytes);

                        this.classes.add(getName(entryBytes));
                    } catch (Exception e) {
                        logger.error("Class %s could not be instrumented: %s", jei.getName(), e);
                    }
                } else if (jei.getName().endsWith(Constants.JAR_FILE_SUFFIX)) {
                    ByteArrayOutputStream innerJarBos = new ByteArrayOutputStream();
                    JarOutputStream innerJos = new JarOutputStream(innerJarBos);
                    JarInputStream innerJis = new JarInputStream(jeis);

                    this.instrumentJarFile(innerJis, innerJos, false);

                    innerJos.flush();
                    innerJos.close();
                    byte[] innerJarBytes = innerJarBos.toByteArray();
                    JarEntry jeo = createJarEntry(jei.getName(), innerJarBytes);
//                    if (root) {
//                        jeo.setCompressedSize(innerJarBytes.length);
//                    }
//                    jeo.setComment("UNPACK:" + DigestUtils.sha1Hex(innerJarBytes));
                    jos.putNextEntry(jeo);

                    copySingleEntry(new ByteArrayInputStream(innerJarBytes), jos);

                    innerJos.close();
//                    JarEntry jeo = createJarEntry(jei.getName(), entryBytes);
//                    jos.putNextEntry(jeo);
//                    jos.write(entryBytes);
                } else {
                    JarEntry jeo = createJarEntry(jei.getName(), entryBytes);
                    jos.putNextEntry(jeo);
                    copySingleEntry(jeis, jos);
                }
                jeis.close();
            } else {
                JarEntry jeo = createJarEntry(jei.getName(), null);
                jos.putNextEntry(jeo);
            }
            jos.closeEntry();
        }
    }

    private String getName(byte[] classBytes) {
        return new ClassReader(classBytes).getClassName();
    }

    private static JarEntry createJarEntry(String name, byte[] bytes) {
        JarEntry jeo = new JarEntry(name);

        if (bytes != null) {
            long crc32 = calculateCrc32(bytes);
            jeo.setCrc(crc32);
            jeo.setSize(bytes.length);
        } else {
            jeo.setSize(0);
            jeo.setCrc(0);
        }

        return jeo;
    }

    private static long calculateCrc32(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.rewind();
        crc32.update(buffer);
        return crc32.getValue();
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

//            jos.setLevel(Deflater.NO_COMPRESSION);
            jos.setMethod(ZipEntry.STORED);

            this.instrumentJarFile(jis, jos, true);
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
