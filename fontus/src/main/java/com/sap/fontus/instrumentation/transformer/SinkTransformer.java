package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Sink;
import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SinkTransformer extends SourceOrSinkTransformer implements ParameterTransformation, ReturnTransformation {
    private static final Logger logger = LogUtils.getLogger();
    private static final FunctionCall defaultTaintChecker = new FunctionCall(Opcodes.INVOKESTATIC,
            Constants.TaintHandlerQN,
            Constants.TaintHandlerCheckTaintName,
            Constants.TaintHandlerCheckTaintDesc,
            false);

    private final Sink sink;
    private final InstrumentationHelper instrumentationHelper;

    public SinkTransformer(Sink sink, InstrumentationHelper instrumentationHelper, int usedLocalVars) {
        super(usedLocalVars);
        this.sink = sink;
        this.instrumentationHelper = instrumentationHelper;
    }

    @Override
    public void transformParameter(int index, String type, MethodTaintingVisitor mv) {

        if (this.sink == null) {
            return;
        }

        // Sink checks
        logger.debug("Type: {}", type);
        // Check whether this parameter needs to be checked for taint
        if (this.sink.findParameter(index) != null) {
            String instrumentedType = this.instrumentationHelper.instrumentQN(type);
            logger.info("Adding taint check for sink {}, parameter {} ({})", this.sink.getName(), index, type);

            // Put the owning object instance onto the stack
            MethodVisitor originalVisitor = mv.getParent();
            this.addParentObjectToStack(originalVisitor, this.sink.getFunction());

            // Add string information about the sink
            originalVisitor.visitLdcInsn(this.sink.getFunction().getFqn());
            originalVisitor.visitLdcInsn(this.sink.getName());

            // Get the source taint handler from the configuration file
            FunctionCall taint = this.sink.getTaintHandler();

            // Add default values if not already defined
            if (taint.isEmpty()) {
                taint = defaultTaintChecker;
            } else if (!IASTaintHandler.isValidTaintChecker(taint)) {
                throw new RuntimeException("Invalid Taint Checker " + taint + " in configuration file (need descriptor: " + Constants.TaintHandlerCheckTaintDesc + ")");
            }
            originalVisitor.visitMethodInsn(taint.getOpcode(), taint.getOwner(), taint.getName(), taint.getDescriptor(), taint.isInterface());
            originalVisitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(instrumentedType).getInternalName());
        }
    }

    @Override
    public boolean requireParameterTransformation(int index, String type) {
        if (this.sink == null) {
            return false;
        }
        return (this.sink.findParameter(index) != null);
    }

    @Override
    public void transformReturnValue(MethodTaintingVisitor mv, Descriptor desc) {
        // Also add a transformation for return types
        this.transformParameter(-1, desc.getReturnType(), mv);
    }

    @Override
    public boolean requiresReturnTransformation(Descriptor desc) {
        if (this.sink == null) {
            return false;
        }
        return (this.sink.findParameter(-1) != null);
    }

    @Override
    public boolean requireParameterVariableLocals() {
        return true;
    }
}
