package de.tubs.cs.ias.asm_test.transformer;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.FunctionCall;
import de.tubs.cs.ias.asm_test.MethodTaintingVisitor;
import de.tubs.cs.ias.asm_test.config.Source;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class SourceTransformer implements ReturnTransformation {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Source source;

    public SourceTransformer(Source source) {
        this.source = source;
    }

    @Override
    public void transform(MethodTaintingVisitor visitor, Descriptor desc) {
        FunctionCall fc = this.source.getFunction();
        logger.info("{}.{}{} is a source, so tainting String by calling {}.tainted!", fc.getOwner(), fc.getName(), fc.getDescriptor(), Constants.TStringQN);

        FunctionCall tainter = new FunctionCall(Opcodes.INVOKESTATIC,
                Constants.TStringQN,
                "tainted",
                Constants.CreateTaintedStringDesc,
                false);
        visitor.visitMethodInsn(tainter);
    }

}
