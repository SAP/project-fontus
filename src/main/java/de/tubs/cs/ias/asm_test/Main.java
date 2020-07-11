package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import de.tubs.cs.ias.asm_test.config.TaintMethod;
import de.tubs.cs.ias.asm_test.instrumentation.Instrumenter;
import de.tubs.cs.ias.asm_test.utils.JdkClassesLookupTable;
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

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.ConfigurationLoader;

@CommandLine.Command(
        description = "Replaces all String instances with taint-aware Strings.",
        name = "asm_taint",
        mixinStandardHelpOptions = true,
        version = "asm_taint 0.0.1"
)
public final class Main implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int OneKB = 1024;
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

    @CommandLine.Option(
            names = {"-c", "--config"},
            required = false,
            paramLabel = "Config",
            description = "Configuration file"
    )
    private File configFile;

    @CommandLine.Option(
            names = {"-t", "--taintmethod"},
            required = false,
            paramLabel = "Taint method",
            description = "Taint method, which should be used. Valid values:  ${COMPLETION-CANDIDATES}",
            defaultValue = TaintMethod.defaultTaintMethodName
    )
    private TaintMethod taintMethod;

    private Configuration configuration;

    private Main() {
        this.instrumenter = new Instrumenter();
    }


    private void instrumentClassStream(InputStream i, OutputStream o) throws IOException {
        byte[] out = this.instrumenter.instrumentClass(i, new ClassResolver(ClassLoader.getSystemClassLoader()), this.configuration);
        o.write(out);
    }

    private static void copySingleEntry(InputStream i, OutputStream o) throws IOException {
        int len = 0;
        byte[] buffer = new byte[OneKB];

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
                        !JdkClassesLookupTable.getInstance().isJdkClass(jei.getName()) &&
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

    private void loadConfiguration() {
        this.configuration = ConfigurationLoader.loadAndMergeConfiguration(this.configFile, this.taintMethod);
    }

    @Override
    public Void call() throws IOException {
        this.loadConfiguration();
        this.walkFileTree(this.inputFile, this.outputFile);
        return null;
    }

    public static void main(String[] args) {
        new CommandLine(new Main())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }

}
