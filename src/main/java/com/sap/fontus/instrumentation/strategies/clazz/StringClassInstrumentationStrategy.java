package com.sap.fontus.instrumentation.strategies.clazz;

import com.sap.fontus.Constants;
import com.sap.fontus.TriConsumer;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;
import java.util.regex.Matcher;

public class StringClassInstrumentationStrategy  extends AbstractClassInstrumentationStrategy {
    private static final Logger logger = LogUtils.getLogger();

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
