package com.sap.fontus.utils.offline;

import com.sap.fontus.Constants;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Pair;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.stream.StreamSupport;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

public class OfflineJarInstrumenter {
    private static final Logger logger = LogUtils.getLogger();

    private final List<String> classes = new ArrayList<>();
    private final OfflineClassInstrumenter offlineClassInstrumenter;

    public OfflineJarInstrumenter(Configuration configuration) {
        this.offlineClassInstrumenter = new OfflineClassInstrumenter(configuration);
    }

    public List<String> getClasses() {
        return classes;
    }

    private Pair<JarEntry, byte[]> processJarEntry(Pair<JarEntry, byte[]> input) {
        JarEntry jei = input.x;
        byte[] entryBytes = input.y;

        logger.info("Processing jar entry: {}", jei.getName());

        if (!jei.isDirectory()) {
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

                    this.classes.add(getName(entryBytes));
                    return new Pair<>(jeo, bytes);
                } catch (Exception e) {
                    logger.error("Class %s could not be instrumented: %s", jei.getName(), e);
                    JarEntry jeo = createJarEntry(jei.getName(), entryBytes);
                    return new Pair<>(jeo, entryBytes);
                }
            } else if (jei.getName().endsWith(Constants.JAR_FILE_SUFFIX)) {
                try {
                    ByteArrayOutputStream innerJarBos = new ByteArrayOutputStream();
                    JarOutputStream innerJos = new JarOutputStream(innerJarBos);
                    JarInputStream innerJis = new JarInputStream(jeis);

                    this.instrumentJarFile(innerJis, innerJos);

                    innerJos.flush();
                    innerJos.close();

                    byte[] innerJarBytes = innerJarBos.toByteArray();
                    JarEntry jeo = createJarEntry(jei.getName(), innerJarBytes);

                    return new Pair<>(jeo, innerJarBytes);
                } catch (IOException ignored) {
                    JarEntry jeo = createJarEntry(jei.getName(), entryBytes);
                    return new Pair<>(jeo, entryBytes);
                }
            } else {
                JarEntry jeo = createJarEntry(jei.getName(), entryBytes);
                return new Pair<>(jeo, entryBytes);
            }
        } else {
            JarEntry jeo = createJarEntry(jei.getName(), null);

            return new Pair<>(jeo, null);
        }
    }

    private void instrumentJarFile(JarInputStream jis, JarOutputStream jos) throws IOException {
        if (Configuration.getConfiguration().isParallel()) {
//            ParallelInstrumenter parallelInstrumenter = new ParallelInstrumenter(jis, jos, this::processJarEntry);
//            parallelInstrumenter.execute();

            StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(new JarInputStreamIterator(jis), Spliterator.IMMUTABLE), true)
                    .map(this::processJarEntry)
                    .forEach(new JarOutputStreamWriter(jos));
        } else {
            JarInputStreamIterator jisi = new JarInputStreamIterator(jis);
            JarOutputStreamWriter josw = new JarOutputStreamWriter(jos);

            while (jisi.hasNext()) {
                Pair<JarEntry, byte[]> input = jisi.next();

                Pair<JarEntry, byte[]> output = this.processJarEntry(input);

                josw.accept(output);
            }
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
                JarOutputStreamWriter.copySingleEntry(manifestInputStream, jos);
                jos.closeEntry();
            }

            jos.setMethod(ZipEntry.STORED);

            this.instrumentJarFile(jis, jos);
        }

        jos.flush();
        jos.close();

        logger.info("Writing transformed jar file to: {}", output.getAbsolutePath());
    }
}
