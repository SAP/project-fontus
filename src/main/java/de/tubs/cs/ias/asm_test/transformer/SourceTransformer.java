package de.tubs.cs.ias.asm_test.transformer;

import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;
import de.tubs.cs.ias.asm_test.instrumentation.MethodTaintingVisitor;
import de.tubs.cs.ias.asm_test.config.Source;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class SourceTransformer implements ReturnTransformation {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Source source;
    private final TaintStringConfig taintStringConfig;

    public SourceTransformer(Source source, TaintStringConfig configuration) {
        this.source = source;
        this.taintStringConfig = configuration;
    }

    @Override
    public void transform(MethodTaintingVisitor visitor, Descriptor desc) {
        FunctionCall fc = this.source.getFunction();
        logger.info("{}.{}{} is a source, so tainting String by calling {}.tainted!", fc.getOwner(), fc.getName(), fc.getDescriptor(), this.taintStringConfig.getTStringQN());

        FunctionCall tainter = new FunctionCall(Opcodes.INVOKESTATIC,
                this.taintStringConfig.getTStringQN(),
                "tainted",
                this.taintStringConfig.getCreateTaintedStringDesc(),
                false);
        visitor.visitMethodInsn(tainter);
    }

}
