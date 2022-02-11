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
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.Opcodes;
import com.sap.fontus.utils.LogUtils;

public class SourceTransformer extends SourceOrSinkTransformer implements ReturnTransformation, ParameterTransformation {
    private static final Logger logger = LogUtils.getLogger();

    private final Source source;


    public SourceTransformer(Source source, int usedLocalVars) {
        super(usedLocalVars);
        this.source = source;
    }

    @Override
    public void transform(MethodTaintingVisitor visitor, Descriptor desc) {
        FunctionCall fc = this.source.getFunction();
        logger.info("{}.{}{} is a source, so tainting String by calling {}.tainted!", fc.getOwner(), fc.getName(), fc.getDescriptor(), Type.getInternalName(IASString.class));

        IASTaintSource source = IASTaintSourceRegistry.getInstance().getOrRegisterObject(this.source.getName());

        // Stack will already contain return value from the source function
        // Stack: return obj,

        // Now add the object on which the method was called
        // Stack: return obj --> return obj, method obj
        this.addParentObjectToStack(visitor, fc);

        // And create an array containing input parameters
        // Stack: return obj, method obj --> return obj, method obj, parameter array
        this.pushParameterArrayOntoStack(visitor, fc.getParsedDescriptor(), this.source.getPassLocals());

        // Now the source id
        // Stack: return obj, method obj, parameter array --> return obj, method obj, parameter array, int
        MethodTaintingUtils.pushNumberOnTheStack(visitor, source.getId());

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
        visitor.visitMethodInsn(taint);
        visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(desc.getReturnType()).getInternalName());
    }

    @Override
    public void transform(int index, String type, MethodTaintingVisitor visitor) {
        // Deliberately leave this as a NOP in order to make sure method parameters are saved to local variables
    }
}
