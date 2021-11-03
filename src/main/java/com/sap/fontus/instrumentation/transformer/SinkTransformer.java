package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.config.Sink;
import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SinkTransformer implements ParameterTransformation {
    private static final Logger logger = LogUtils.getLogger();

    private final Sink sink;
    private final InstrumentationHelper instrumentationHelper;
    private final int usedLocalVars;

    public SinkTransformer(Sink sink, InstrumentationHelper instrumentationHelper, int usedLocalVars) {
        this.sink = sink;
        this.instrumentationHelper = instrumentationHelper;
        this.usedLocalVars = usedLocalVars;
    }

    @Override
    public void transform(int index, String type, MethodTaintingVisitor mv) {

        if (this.sink == null) {
            return;
        }

        // Sink checks
        logger.debug("Type: {}", type);
        // Check whether this parameter needs to be checked for taint
        if (this.sink.findParameter(index) != null) {
            String instrumentedType = instrumentationHelper.instrumentQN(type);
            logger.info("Adding taint check for sink {}, parameter {} ({})", this.sink.getName(), index, type);
            String sinkFunction = String.format("%s.%s%s", this.sink.getFunction().getOwner(), this.sink.getFunction().getName(), this.sink.getFunction().getDescriptor());
            String sinkName = this.sink.getName();

            int size = this.sink.getFunction().getParsedDescriptor().getParameters().stream().mapToInt((param) -> Type.getType(param).getSize()).sum();

            // Put the owning object instance onto the stack
            MethodVisitor originalVisitor = mv.getParent();
            if (this.sink.getFunction().isInstanceMethod()) {
                originalVisitor.visitVarInsn(Type.getObjectType(this.sink.getFunction().getOwner()).getOpcode(Opcodes.ILOAD), size + this.usedLocalVars);
            } else {
                originalVisitor.visitInsn(Opcodes.ACONST_NULL);
            }

            originalVisitor.visitLdcInsn(sinkFunction);
            originalVisitor.visitLdcInsn(sinkName);
            originalVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TaintHandlerQN, Constants.TaintHandlerCheckTaintName, Constants.TaintHandlerCheckTaintDesc, false);
            originalVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(instrumentedType).getInternalName());
        }
    }
}
