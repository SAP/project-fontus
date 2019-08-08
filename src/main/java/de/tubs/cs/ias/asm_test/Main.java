package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
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
public class Main implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Instrumenter instrumenter;

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
            names = {"-a", "--add"},
            paramLabel = "Add taint-aware classes",
            description = "Adds the class files of our taint-aware data types to the instrumented .jar file."
    )
    private boolean addTaintAwareClassFiles;

    private static final String TStringClassName = toClassName(Constants.TStringQN);
    private static final String TStringBuilderClassName = toClassName(Constants.TStringBuilderQN);
    private static final String TStringBufferClassName = toClassName(Constants.TStringBufferQN);
    private static final String TReflectionProxies = toClassName("de/tubs/cs/ias/asm_test/IASReflectionProxies");

    private static final String classSuffix = ".class";
    private static final String jarSuffix = ".jar";
    private static final List<String> TStringTypesClassNames = Arrays.asList(
            TStringClassName,
            TStringBuilderClassName,
            TReflectionProxies,
            TStringBufferClassName
    );

    private Main() {
        this.instrumenter = new Instrumenter();
    }


    private void instrumentClassStream(InputStream i, OutputStream o) throws IOException {
        byte[] out = this.instrumenter.instrumentClass(i);
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
        String jarPath = getPathToCurrentJar();
        File f = new File(jarPath);
        try (JarFile ji = new JarFile(input); JarFile currJar = new JarFile(f)) {

            FileOutputStream fos = new FileOutputStream(output);
            jos = new JarOutputStream(fos);

            if (this.addTaintAwareClassFiles) {
                copyTaintAwareClassFiles(jos, ji, currJar);
            }

            logger.info("Reading jar file from: {}", input.getAbsolutePath());

            for (Enumeration<JarEntry> e = ji.entries(); e.hasMoreElements(); ) {
                JarEntry jei = e.nextElement();
                JarEntry jeo = new JarEntry(jei.getName());
                InputStream jeis = ji.getInputStream(jei);

                logger.info("Reading jar entry: {}", jei.getName());

                jos.putNextEntry(jeo);

                // Skip the taint aware string types so we don't mess them up by instrumenting them again!
                if (jei.getName().endsWith(classSuffix) && !TStringTypesClassNames.contains(jei.getName())) {
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

    private static void copyTaintAwareClassFiles(JarOutputStream jos, JarFile ji, JarFile currJar) throws IOException {
        logger.info("Adding required class files to jar..");

        List<JarEntry> entriesToAdd = getJarEntriesToCopy(currJar);
        for (JarEntry je : entriesToAdd) {
            if (ji.getJarEntry(je.getName()) != null) {
                logger.info("{} is already contained in jar, skipping..", je.getName());
                continue;
            }
            logger.info("Adding jar entry: {}", je.getName());
            InputStream currJarIn = currJar.getInputStream(je);
            JarEntry ne = new JarEntry(je.getName());
            jos.putNextEntry(ne);
            copySingleEntry(currJarIn, jos);
            currJarIn.close();
            jos.closeEntry();
        }
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
        if (input.getName().endsWith(classSuffix)) {
            instrumentClassFile(input, output);
        } else if (input.getName().endsWith(jarSuffix)) {
            this.instrumentJarFile(input, output);
        } else if (input.isDirectory()) {
            this.instrumentDirectory(input, output);
        } else {
            logger.error("Input file name must have class or jar extension!");
        }
    }

    @Override
    public Void call() throws IOException {
        this.walkFileTree(this.inputFile, this.outputFile);
        return null;
    }

    private static boolean isJarEntryToCopy(String name) {
        for (String e : TStringTypesClassNames) {
            if (e.equals(name)) return true;
        }
        return false;
    }

    private static List<JarEntry> getJarEntriesToCopy(JarFile ji) {

        List<JarEntry> entries = new ArrayList<>();
        for (Enumeration<JarEntry> e = ji.entries(); e.hasMoreElements(); ) {
            JarEntry jei = e.nextElement();
            if (isJarEntryToCopy(jei.getName())) {
                entries.add(jei);
            }
        }
        return entries;
    }

    private static String getPathToCurrentJar() throws UnsupportedEncodingException {
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return URLDecoder.decode(path, "UTF-8"); // Constant as the Enum requires JDK10 or above
    }

    private static String toClassName(String classNameQn) {
        return String.format("%s%s", classNameQn, classSuffix);
    }

    public static void main(String[] args) {
        CommandLine.call(new Main(), args);
    }

}
