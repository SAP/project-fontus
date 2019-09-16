package de.tubs.cs.ias.asm_test.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.strategies.StringInstrumentation;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.regex.Matcher;

public class StringClassInstrumentationStrategy extends StringInstrumentation implements ClassInstrumentationStrategy{
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ClassVisitor cv;
    public StringClassInstrumentationStrategy(ClassVisitor cv) {
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
            return Optional.of(this.cv.visitField(access, name, newDescriptor, signature, null));
        }
        return Optional.empty();
    }
}
