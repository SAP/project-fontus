import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import picocli.CommandLine;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@CommandLine.Command(description = "Replaces all String instances with taintable Strings.",
         name = "asm_taint", mixinStandardHelpOptions = true, version = "asm_taint 0.0.1")
public class Main implements Callable<Void> {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @CommandLine.Option(names = { "-f", "--file" }, paramLabel = "Input", description = "The input class file")
    private
    File inputFile;

    @CommandLine.Option(names = { "-o", "--out" }, paramLabel = "Output", description = "The output class file")
    private
    File outputFile;

    @Override
    public Void call() throws IOException {

        try(FileInputStream fi = new FileInputStream(this.inputFile);
            FileOutputStream fo = new FileOutputStream(this.outputFile)) {
            LOGGER.info(String.format("Reading class file from: %s", this.inputFile.getAbsolutePath()));
            ClassReader cr = new ClassReader(fi);
            ClassWriter writer = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            //ClassVisitor cca = new CheckClassAdapter(writer);
            StringMethodRewriter smr = new StringMethodRewriter(writer);
            cr.accept(smr, 0);
            LOGGER.info(String.format("Writing transformed class file to: %s", this.outputFile.getAbsolutePath()));
            fo.write(writer.toByteArray());
        }
        return null;
    }
    public static void main(String[] args) {
        CommandLine.call(new Main(), args);
    }
}
