package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.util.Optional;
import java.util.regex.Matcher;

public class StringClassInstrumentationStrategy  extends AbstractClassInstrumentationStrategy {
    private static final ParentLogger logger = LogUtils.getLogger();

    public StringClassInstrumentationStrategy(ClassVisitor cv, TaintStringConfig configuration) {
        super(cv, Constants.StringDesc, configuration.getTStringDesc(), Constants.StringQN, Constants.TStringToStringName);
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        Matcher descMatcher = this.descPattern.matcher(descriptor);
        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(this.taintedDesc);
            logger.info("Replacing String field [{}]{}.{} with [{}]{}.{}", access, name, descriptor, access, name, newDescriptor);
            if (value != null && (access & Opcodes.ACC_FINAL) != 0 && (access & Opcodes.ACC_STATIC) != 0) {
                tc.apply(name, descriptor, value);
            }
            return Optional.of(this.visitor.visitField(access, name, newDescriptor, signature, null));
        }
        return Optional.empty();
    }
}
