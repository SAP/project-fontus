package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.ClassTaintingVisitor;
import de.tubs.cs.ias.asm_test.Configuration;
import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.JdkClassesLookupTable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.security.ProtectionDomain;

public class TaintAgent {
    public static void premain(String args, Instrumentation inst) {
        inst.addTransformer(new TaintAgent.TaintingTransformer());
    }

    static class TaintingTransformer implements ClassFileTransformer {
        private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        Configuration config = Configuration.instance;
        JdkClassesLookupTable jdkClasses = JdkClassesLookupTable.instance;
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            if(jdkClasses.isJdkClass(className) || className.startsWith("jdk") || className.startsWith("java")) {
                logger.info("Skipping JDK class: {}", className);
                return classfileBuffer;
            }
            if(className.startsWith(Constants.TPackage)) {
                logger.info("Skipping Tainting Framework class: {}", className);
                return classfileBuffer;
            }

            logger.info("Tainting class: {}", className);
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            //ClassVisitor cca = new CheckClassAdapter(writer);
            ClassTaintingVisitor smr = new ClassTaintingVisitor(writer);
            cr.accept(smr, ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        }

    }
}
