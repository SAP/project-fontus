package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Source;
import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.MethodTaintingUtils;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.stats.Statistics;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.Opcodes;
import com.sap.fontus.utils.LogUtils;

public class SourceTransformer extends SourceOrSinkTransformer implements ReturnTransformation {
    private static final Logger logger = LogUtils.getLogger();

    private final Source source;

    public SourceTransformer(Source source, int usedLocalVars, FunctionCall caller) {
        super(usedLocalVars, caller);
        this.source = source;
    }

    @Override
    public void transformReturnValue(MethodTaintingVisitor mv, Descriptor desc) {
        FunctionCall fc = this.source.getFunction();
        logger.info("{}.{}{} is a source, so tainting String by calling {}.tainted!", fc.getOwner(), fc.getName(), fc.getDescriptor(), Type.getInternalName(IASString.class));

        IASTaintSource source = IASTaintSourceRegistry.getInstance().getOrRegisterObject(this.source.getName());

        // Stack will already contain return value from the source function
        // Stack: return obj,

        // Now add the object on which the method was called
        // Stack: return obj --> return obj, method obj
        this.addParentObjectToStack(mv, fc);

        // And create an array containing input parameters
        // Stack: return obj, method obj --> return obj, method obj, parameter array
        this.pushParameterArrayOntoStack(mv, fc.getParsedDescriptor(), this.source.getPassLocals());

        // Now the source id
        // Stack: return obj, method obj, parameter array --> return obj, method obj, parameter array, int
        MethodTaintingUtils.pushNumberOnTheStack(mv, source.getId());

        // Add the caller name
        MethodVisitor originalVisitor = mv.getParent();
        originalVisitor.visitLdcInsn(this.getCaller().getFqn());

        // Get the source taint handler from the configuration file
        FunctionCall taint = this.source.getTaintHandler();

        // Add default values if not already defined
        if (taint.isEmpty()) {
            taint = new FunctionCall(Opcodes.INVOKESTATIC,
                    Constants.TaintHandlerQN,                             // IASTaintHandler Class
                    Constants.TaintHandlerTaintName,                      // taint Method
                    Constants.TaintHandlerTaintDesc,                      // Object taint(Object object, Object parentObject, Object[] parameters, int sourceId)
                    false);
        } else if (!IASTaintHandler.isValidTaintHandler(taint)) {
            throw new RuntimeException("Invalid Taint Handler in configuration file!");
        }

        // Call the handler:
        // Stack: return obj, method obj, parameter array, int --> tainted return obj
        mv.visitMethodInsn(taint);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(desc.getReturnType()).getInternalName());

        // Statistics
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.addNewSink(this.source.getFunction().getFqn(), this.getCaller().getFqn());
        }
    }

    @Override
    public boolean requiresReturnTransformation(Descriptor desc) {
        return true;
    }

    @Override
    public boolean requireParameterVariableLocals() {
        // Here we need the method inputs to be added as local parameters
        return true;
    }

}
