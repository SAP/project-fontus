package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "Replaces all String instances with taint-aware Strings.",
        name = "asm_taint", mixinStandardHelpOptions = true, version = "asm_taint 0.0.1")
public class Main implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @CommandLine.Option(names = {"-f", "--file"}, required = true, paramLabel = "Input", description = "The input class/jar file")
    private File inputFile;

    @CommandLine.Option(names = {"-o", "--out"}, required = true, paramLabel = "Output", description = "The output class/jar file")
    private File outputFile;

    @CommandLine.Option(names = {"-c", "--check"}, paramLabel = "Check Requirements", description = "Check whether the required class files are present.")
    private boolean checkRequirements;

    @CommandLine.Option(names = {"-a", "--add"}, paramLabel = "Add taint-aware classes", description = "Adds the class files of our taint-aware data types to the instrumented .jar file.")
    private boolean addTaintAwareClassFiles;

    private static final String TStringClassName = "de/tubs/cs/ias/asm_test/IASString.class";
    private static final String TStringBuilderClassName = "de/tubs/cs/ias/asm_test/IASStringBuilder.class";

    private static final List<String> TStringTypesClassNames = Arrays.asList(TStringClassName, TStringBuilderClassName);

    /**
     * Checks whether all the supporting files are present in the output directory.
     * TODO: fix this to load them properly.
     *
     * @return Whether we can proceed
     */
    private boolean checkRequirements() {
        File outDir = new File(this.outputFile.getParent());
        if (!outDir.isDirectory()) {
            return false;
        }
        String[] requiredFilenames = {"IASString.class", "IASStringBuilder.class"};
        File instrumentedDir = new File(outDir, Constants.TPackage);
        if (!instrumentedDir.isDirectory()) {
            return false;
        }
        for (String required : requiredFilenames) {
            File requiredFile = new File(instrumentedDir, required);
            if (requiredFile.exists() && requiredFile.isFile()) {
                logger.info("Found required file {} at {}", required, requiredFile.getAbsoluteFile());
            } else {
                logger.error("Did not find required file {} at {}", required, requiredFile.getAbsoluteFile());

                return false;
            }
        }
        return true;
    }

    private void instrumentClassStream(InputStream i, OutputStream o) throws IOException {
        ClassReader cr = new ClassReader(i);
        ClassWriter writer = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        //ClassVisitor cca = new CheckClassAdapter(writer);
        ClassTaintingVisitor smr = new ClassTaintingVisitor(writer);
        cr.accept(smr, 0);
        o.write(writer.toByteArray());
    }

    private void copySingleEntry(InputStream i, OutputStream o) throws IOException {
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

            if(this.addTaintAwareClassFiles) {
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
                    this.copySingleEntry(currJarIn, jos);
                    currJarIn.close();
                    jos.closeEntry();
                }
            }

            logger.info("Reading jar file from: {}", input.getAbsolutePath());

            for (Enumeration<JarEntry> e = ji.entries(); e.hasMoreElements(); ) {
                JarEntry jei = e.nextElement();
                JarEntry jeo = new JarEntry(jei.getName());
                InputStream jeis = ji.getInputStream(jei);

                logger.info("Reading jar entry: {}", jei.getName());

                jos.putNextEntry(jeo);

                // Skip the taint aware string types so we don't mess them up by instrumenting them again!
                if (jei.getName().endsWith(".class") && !TStringTypesClassNames.contains(jei.getName())) {
                    this.instrumentClassStream(jeis, jos);
                } else {
                    this.copySingleEntry(jeis, jos);
                }
                jeis.close();
                jos.closeEntry();
            }
        }

        jos.close();

        logger.info("Writing transformed jar file to: {}", output.getAbsolutePath());
    }

    @Override
    public Void call() throws IOException {
        if (this.checkRequirements && !this.checkRequirements()) {
            logger.error("Required support files do not exist, can't proceed!");
            return null;
        }

        if (this.inputFile.getName().endsWith(".class")) {
            this.instrumentClassFile(this.inputFile, this.outputFile);
        } else if (this.inputFile.getName().endsWith(".jar")) {
            this.instrumentJarFile(this.inputFile, this.outputFile);
        } else {
            logger.error("Input file name must have class or jar extension!");
        }

        return null;
    }

    private static boolean isJarEntryToCopy(String name) {
        String[] toCopy = { /* "de/", "de/tubs/", "de/tubs/cs/", "de/tubs/cs/ias/", "de/tubs/cs/ias/asm_test/", */ "de/tubs/cs/ias/asm_test/IASStringBuilder.class", "de/tubs/cs/ias/asm_test/IASString.class"};
        for (String e : toCopy) {
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
        return URLDecoder.decode(path, "UTF-8");
    }

    public static void main(String[] args) throws IOException {
        CommandLine.call(new Main(), args);
    }
}
