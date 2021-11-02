package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.config.Source;
import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.MethodTaintingUtils;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.Opcodes;
import com.sap.fontus.utils.LogUtils;

public class SourceTransformer implements ReturnTransformation {
    private static final Logger logger = LogUtils.getLogger();

    private final Source source;

    public SourceTransformer(Source source) {
        this.source = source;
    }


    @Override
    public void transform(MethodTaintingVisitor visitor, Descriptor desc) {
        FunctionCall fc = this.source.getFunction();
        logger.info("{}.{}{} is a source, so tainting String by calling {}.tainted!", fc.getOwner(), fc.getName(), fc.getDescriptor(), Type.getInternalName(IASString.class));

        IASTaintSource source = IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource(this.source.getName());

        MethodTaintingUtils.pushNumberOnTheStack(visitor, source.getId());

        // TODO: Make this configurable
        FunctionCall taint = new FunctionCall(Opcodes.INVOKESTATIC,
                Constants.TaintHandlerQN,                             // IASTaintHandler Class
                Constants.TaintHandlerTaintName,                      // taint Method
                Constants.TaintHandlerTaintDesc,                      // Object taint(Object object, int sourceId)
                false);
        visitor.visitMethodInsn(taint);
        visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(desc.getReturnType()).getInternalName());
    }
}
