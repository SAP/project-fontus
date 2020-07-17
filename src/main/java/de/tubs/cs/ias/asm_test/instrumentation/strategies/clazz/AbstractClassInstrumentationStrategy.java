package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.AbstractInstrumentation;
import jdk.internal.org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractClassInstrumentationStrategy implements ClassInstrumentationStrategy {
    private static final ParentLogger logger = LogUtils.getLogger();
    protected final ClassVisitor visitor;
    protected final Pattern descPattern;
    protected final String taintedDesc;
    protected final String origQN;
    protected final String getOriginalTypeMethod;

    AbstractClassInstrumentationStrategy(ClassVisitor visitor, String origDesc, String taintedDesc, String origQN, String taintedQN, String getOriginalTypeMethod) {
        this.visitor = visitor;
        this.descPattern = Pattern.compile(origDesc);
        this.origQN = origQN;
        this.taintedDesc = taintedDesc;
        this.getOriginalTypeMethod = getOriginalTypeMethod;
    }

    AbstractClassInstrumentationStrategy(ClassVisitor visitor, Class<?> clazz, String taintedDesc, String taintedQN, String getOriginalTypeMethod) {
        this.visitor = visitor;
        this.origQN = Type.getType(clazz).getInternalName();
        String origDesc = Type.getType(clazz).getDescriptor();
        this.descPattern = Pattern.compile(origDesc);
        this.taintedDesc = taintedDesc;
        this.getOriginalTypeMethod = getOriginalTypeMethod;
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        Matcher descMatcher = this.descPattern.matcher(descriptor);
        if (descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(this.taintedDesc);
            logger.info("Replacing {} field [{}]{}.{} with [{}]{}.{}", this.origQN, access, name, descriptor, access, name, newDescriptor);
            return Optional.of(this.visitor.visitField(access, name, newDescriptor, signature, value));
        }
        return Optional.empty();
    }
}
