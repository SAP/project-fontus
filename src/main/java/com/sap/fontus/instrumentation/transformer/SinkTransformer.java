package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.config.Sink;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.instrumentation.strategies.InstrumentationHelper;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SinkTransformer implements ParameterTransformation {
    private static final Logger logger = LogUtils.getLogger();

    private final Sink sink;
    private final TaintStringConfig config;

    public SinkTransformer(Sink sink, TaintStringConfig configuration) {
        this.sink = sink;
        this.config = configuration;
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
            String instrumentedType = InstrumentationHelper.getInstance(this.config).instrumentQN(type);
            logger.info("Adding taint check for sink {}, paramater {} ({})", this.sink.getName(), index, type);
            String sinkName = String.format("%s.%s%s", this.sink.getFunction().getOwner(), this.sink.getFunction().getName(), this.sink.getFunction().getDescriptor());
            String sink = this.sink.getCategory() == null ? "unknown" : this.sink.getCategory();

            MethodVisitor originalVisitor = mv.getParent();
            originalVisitor.visitLdcInsn(sinkName);
            originalVisitor.visitLdcInsn(sink);
            originalVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TaintHandlerQN, Constants.TaintHandlerCheckTaintName, Constants.TaintHandlerCheckTaintDesc, false);
            originalVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(instrumentedType).getInternalName());
        }
    }
}
