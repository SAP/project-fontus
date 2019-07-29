package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.regex.Matcher;

class StringClassInstrumentationStrategy implements ClassInstrumentationStrategy{
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ClassVisitor cv;
    StringClassInstrumentationStrategy(ClassVisitor cv) {
        this.cv = cv;
    }


    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        assert this.cv != null;
        Matcher descMatcher = Constants.strPattern.matcher(descriptor);
        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            logger.info("Replacing String field [{}]{}.{} with [{}]{}.{}", access, name, descriptor, access, name, newDescriptor);
            if (value != null && access == (Opcodes.ACC_FINAL | Opcodes.ACC_STATIC)) {
                tc.apply(name, descriptor, value);
            }
            return Optional.of(this.cv.visitField(access, name, newDescriptor, signature, value));
        }
        return Optional.empty();
    }

    @Override
    public Descriptor instrumentMethodInvocation(Descriptor desc) {
        return desc.replaceType(Constants.StringDesc, Constants.TStringDesc);
    }
}
