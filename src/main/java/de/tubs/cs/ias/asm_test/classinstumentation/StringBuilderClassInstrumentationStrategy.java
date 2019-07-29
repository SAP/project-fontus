package de.tubs.cs.ias.asm_test.classinstumentation;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.TriConsumer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.regex.Matcher;

public class StringBuilderClassInstrumentationStrategy implements ClassInstrumentationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ClassVisitor visitor;
    public StringBuilderClassInstrumentationStrategy(ClassVisitor cv) {
        this.visitor = cv;
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        Matcher descMatcher = Constants.strBuilderPattern.matcher(descriptor);
        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(Constants.TStringBuilderDesc);
            logger.info("Replacing StringBuilder field [{}]{}.{} with [{}]{}.{}", access, name, descriptor, access, name, newDescriptor);
            return Optional.of(this.visitor.visitField(access, name, newDescriptor, signature, value));
        }
        return Optional.empty();
    }

    @Override
    public Descriptor instrumentMethodInvocation(Descriptor desc) {
        return desc.replaceType(Constants.StringBuilderDesc, Constants.TStringBuilderDesc);
    }
}