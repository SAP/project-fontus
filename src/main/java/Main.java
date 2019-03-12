import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;


public class Main {

    public static void main(String[] args) throws IOException {


        String path = "/home/leen/Projects/TU_BS/java_bytecode_rewriting/testing/";
        String fnameOut = "TestString.class";
        String fname = "TestString.class";
        File file = new File(path+fname);
        try(FileInputStream fs = new FileInputStream(file)) {

            ClassReader cr = new ClassReader(fs);
            ClassWriter writer = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            //ClassVisitor cca = new CheckClassAdapter(writer);
            //ClassPrinter cp = new ClassPrinter(writer);

            //MethodReplacer r = new MethodReplacer(cp, "getValue", "()I");
            //NumberReplacer nr = new NumberReplacer(r);
            StringMethodRewriter smr = new StringMethodRewriter(writer);
            cr.accept(smr, 0);
            File fileOut = new File(path + "temp/" + fnameOut);
            try (FileOutputStream fsOut = new FileOutputStream(fileOut)) {
                fsOut.write(writer.toByteArray());
            }
        }
    }
}
