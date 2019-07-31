package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.*;
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

        private Configuration config = Configuration.instance;
        private JdkClassesLookupTable jdkClasses = JdkClassesLookupTable.instance;
        private Instrumenter instrumenter;

        TaintingTransformer() {
            this.instrumenter = new Instrumenter();
        }

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
            return this.instrumenter.instrumentClass(classfileBuffer);
        }
    }
}
