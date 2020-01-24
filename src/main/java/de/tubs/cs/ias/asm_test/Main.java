package de.tubs.cs.ias.asm_test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

@CommandLine.Command(
        description = "Replaces all String instances with taint-aware Strings.",
        name = "asm_taint",
        mixinStandardHelpOptions = true,
        version = "asm_taint 0.0.1"
)
public final class Main implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Instrumenter instrumenter;

    @CommandLine.Option(
            names = {"-f", "--file"},
            required = true,
            paramLabel = "Input",
            description = "The input class/jar file or directory"
    )
    private File inputFile;

    @CommandLine.Option(
            names = {"-o", "--out"},
            required = true,
            paramLabel = "Output",
            description = "The output class/jar file or directory"
    )
    private File outputFile;

    private static final JdkClassesLookupTable jdkClasses = JdkClassesLookupTable.instance;

    private Main() {
        this.instrumenter = new Instrumenter();
    }


    private void instrumentClassStream(InputStream i, OutputStream o) throws IOException {
        byte[] out = this.instrumenter.instrumentClass(i, ClassLoader.getSystemClassLoader());
        o.write(out);
    }

    private static void copySingleEntry(InputStream i, OutputStream o) throws IOException {
        int len = 0;
        byte[] buffer = new byte[1024];

        while ((len = i.read(buffer, 0, buffer.length)) != -1) {
            o.write(buffer, 0, len);
        }
    }

    private void instrumentClassFile(File input, File output) throws IOException {
        FileInputStream fi = new FileInputStream(input);
        FileOutputStream fo = new FileOutputStream(output);
        logger.info("Reading class file from: {}", input.getAbsolutePath());
        this.instrumentClassStream(fi, fo);
        logger.info("Writing transformed class file to: {}", output.getAbsolutePath());
    }

    private void instrumentJarFile(File input, File output) throws IOException {
        JarOutputStream jos;
        try (JarFile ji = new JarFile(input)) {
            FileOutputStream fos = new FileOutputStream(output);
            jos = new JarOutputStream(fos);

            logger.info("Reading jar file from: {}", input.getAbsolutePath());

            for (Enumeration<JarEntry> e = ji.entries(); e.hasMoreElements(); ) {
                JarEntry jei = e.nextElement();
                JarEntry jeo = new JarEntry(jei.getName());
                InputStream jeis = ji.getInputStream(jei);

                logger.info("Reading jar entry: {}", jei.getName());

                jos.putNextEntry(jeo);

                if (jei.getName().endsWith(Constants.CLASS_FILE_SUFFIX) &&
                        !jdkClasses.isJdkClass(jei.getName()) &&
                        !jei.getName().startsWith("de/tubs/cs/ias/asm_test/") &&
                        !jei.getName().startsWith("org/slf4j") &&
                        !jei.getName().startsWith("ch/qos/logback") &&
                        !jei.getName().startsWith("module-info") &&
                        !jei.getName().startsWith("org/openjdk/jmh/") &&
                        !jei.getName().startsWith("org/apache/commons/commons-math3") &&
                        !jei.getName().startsWith("net/sf/jopt-simple/")
                ) {
                    this.instrumentClassStream(jeis, jos);
                } else {
                    copySingleEntry(jeis, jos);
                }
                jeis.close();
                jos.closeEntry();
            }
        }

        jos.close();

        logger.info("Writing transformed jar file to: {}", output.getAbsolutePath());
    }

    private void instrumentDirectory(File input, File output) throws IOException {
        // Create the output directory
        if (!output.isDirectory()) {
            if (!output.mkdirs()) {
                logger.error("Error Creating output directory!");
                return;
            }
        }
        for (File f : input.listFiles()) {
            File o = new File(output.getPath() + File.separator + f.getName());
            // Recurse
            this.walkFileTree(f, o);
        }
    }

    private void walkFileTree(File input, File output) throws IOException {
        if (input.isDirectory()) {
            // Some directories end with .jar, so check for directory first
            this.instrumentDirectory(input, output);
        } else if (input.length() == 0L) {
            // There sometimes are .jar files that are empty, handle those..
            logger.info("File of size 0: {}", input.getName());
        } else if (input.getName().endsWith(Constants.CLASS_FILE_SUFFIX)) {
            this.instrumentClassFile(input, output);
        } else if (input.getName().endsWith(Constants.JAR_FILE_SUFFIX)) {
            this.instrumentJarFile(input, output);
        } else {
            logger.error("Input file name must have class or jar extension!");
        }
    }

    @Override
    public Void call() throws IOException {
        this.walkFileTree(this.inputFile, this.outputFile);
        return null;
    }

    public static void main(String[] args) {
        CommandLine.call(new Main(), args);
    }

}
