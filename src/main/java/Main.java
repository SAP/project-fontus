import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "Replaces all String instances with taintable Strings.",
         name = "asm_taint", mixinStandardHelpOptions = true, version = "asm_taint 0.0.1")
public class Main implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @CommandLine.Option(names = { "-f", "--file" }, required = true, paramLabel = "Input", description = "The input class file")
    private File inputFile;

    @CommandLine.Option(names = { "-o", "--out" }, required = true, paramLabel = "Output", description = "The output class file")
    private File outputFile;

    @CommandLine.Option(names = { "-c", "--check"}, paramLabel = "Check Requirements", description = "Check whether the required class files are present.")
    private boolean checkRequirements;

    /**
     * Checks whether all the supporting files are present in the output directory.
     * TODO: fix this to load them properly.
     *
     * @return Whether we can proceed
     */
    private boolean checkRequirements() {
        File outDir = new File(this.outputFile.getParent());
        if(!outDir.isDirectory()) { return false; }
        String[] requiredFilenames = { "IASString.class", "PrintStreamProxies.class"};
        for(String required : requiredFilenames) {
            File requiredFile = new File(outDir, required);
            if(requiredFile.exists() && requiredFile.isFile()) {
                logger.info("Found required file {} at {}", required, requiredFile.getAbsoluteFile());
            } else {
                logger.error("Did not find required file {} at {}", required, requiredFile.getAbsoluteFile());

                return false;
            }
        }
        return true;
    }

    @Override
    public Void call() throws IOException {
        if(this.checkRequirements && !this.checkRequirements()) {
            logger.error("Required support files do not exist, can't proceed!");
            return null;
        }
        try(FileInputStream fi = new FileInputStream(this.inputFile);
            FileOutputStream fo = new FileOutputStream(this.outputFile)) {
            logger.info("Reading class file from: {}", this.inputFile.getAbsolutePath());
            ClassReader cr = new ClassReader(fi);
            ClassWriter writer = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            //ClassVisitor cca = new CheckClassAdapter(writer);
            ClassTaintingVisitor smr = new ClassTaintingVisitor(writer);
            cr.accept(smr, 0);
            logger.info("Writing transformed class file to: {}", this.outputFile.getAbsolutePath());
            fo.write(writer.toByteArray());
        }
        return null;
    }

    public static void main(String[] args) {
        CommandLine.call(new Main(), args);
    }
}
