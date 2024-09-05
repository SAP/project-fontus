package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

public class MethodParameterTransformer {
    private static final Logger logger = LogUtils.getLogger();

    private final MethodTaintingVisitor visitor;
    private final FunctionCall function;
    private final List<ParameterTransformation> paramTransformations;
    private final List<ReturnTransformation> returnTransformations;
    private final Descriptor descriptor;


    public MethodParameterTransformer(MethodTaintingVisitor visitor, FunctionCall function) {
        this.visitor = visitor;
        this.function = function;
        this.paramTransformations = new ArrayList<>();
        this.returnTransformations = new ArrayList<>();
        this.descriptor = Descriptor.parseDescriptor(this.function.getDescriptor());
    }

    public void addParameterTransformation(ParameterTransformation transformation) {
        if (transformation != null) {
            this.paramTransformations.add(transformation);
        }
    }

    public void addReturnTransformation(ReturnTransformation transformation) {
        if (transformation != null) {
            this.returnTransformations.add(transformation);
        }
    }

    public boolean rewriteCheckCast() {
        return this.descriptor.getReturnType().equals(Constants.ObjectDesc);
    }

    public int getExtraStackSlots() {
        return Utils.getArgumentsStackSize(this.descriptor.toDescriptor()) + (this.function.isInstanceMethod() ? Type.getObjectType(this.function.getOwner()).getSize() : 0);
    }

    public boolean needsTransformation() {
        return this.needsParameterTransformation() || this.needsReturnTransformation();
    }

    public boolean needsParameterTransformation() {
        for (ParameterTransformation p : this.paramTransformations) {
            String[] paramList = this.descriptor.getParameters();
            for (int i = 0; i < paramList.length; i++) {
                String pName = paramList[i];
                if (p.requireParameterTransformation(i, pName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean needsParametersAsLocalVariables() {
        for (ReturnTransformation r : this.returnTransformations) {
            if (r.requireParameterVariableLocals()) {
                return true;
            }
        }
        return false;
    }

    public boolean needsReturnTransformation() {
        for (ReturnTransformation r : this.returnTransformations) {
            if (r.requiresReturnTransformation(this.descriptor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Walks through method parameters on the stack, calling Listener.TransformParameter
     * for each parameter. Can be used to transform or check parameters before a method
     * is called.
     * <p>
     * Should be called before an invoke method bytecode operation, and assumes the
     * stack is populated with method parameters.
     * <p>
     * The callback is made on each parameter, before moving the stack varable into
     * local variable storage. Once each parameter is transformed (as necessary), recreates
     * the stack from local variables.
     * <p>
     * Note the caller still needs to invoke the actual method!
     *
     * @param nUsedLocalVariables The number of local variables currently used by the
     * calling method.
     */
    public void modifyStackParameters(int nUsedLocalVariables) {

        if (!this.needsParameterTransformation() && !this.needsParametersAsLocalVariables()) {
            return;
        }

        Deque<String> params = this.descriptor.getParameterStack();
        Deque<Runnable> loadStack = new ArrayDeque<>(params.size());

        int n = nUsedLocalVariables;

        // Store the owning object in the local variables
        if (this.function.isInstanceMethod()) {
            // First store all variables as local variables
            for (int i = params.size() - 1; i >= 0; i--) {
                String p = params.pop();
                Type t = Type.getType(p);

                this.visitor.visitVarInsn(t.getOpcode(Opcodes.ISTORE), n);
                n += t.getSize();
            }

            // Then store the owning object as a local variable
            int ownerN = n;
            this.visitor.visitInsn(Opcodes.DUP);
            this.visitor.visitVarInsn(Type.getObjectType(this.function.getOwner()).getOpcode(Opcodes.ISTORE), ownerN);

            // Reload the local variables onto the stack
            for (String p : this.descriptor.getParameters()) {
                Type t = Type.getType(p);

                n -= t.getSize();

                this.visitor.visitVarInsn(t.getOpcode(Opcodes.ILOAD), n);
            }
        }

        params = this.descriptor.getParameterStack();
        // The current local variable slot
        n = nUsedLocalVariables;
        // The current method parameter, starting at the end (on top of the stack)
        int index = params.size() - 1;
        // With zero parameters, the loop is skipped
        while (!params.isEmpty()) {
            String p = params.pop();
            int storeOpcode = Type.getType(p).getOpcode(Opcodes.ISTORE);
            int loadOpcode = Type.getType(p).getOpcode(Opcodes.ILOAD);

            // Call the transformation callbacks in reverse order!
            for (int i = this.paramTransformations.size() - 1; i >= 0; i--) {
                if(LogUtils.LOGGING_ENABLED) {
                    logger.info("Calling transformation: {} on parameter {} for {}", i, index, p);
                }
                this.paramTransformations.get(i).transformParameter(index, p, this.visitor);
            }

            // If there is just one parameter, can skip storing and loading stack
            if (this.descriptor.parameterCount() == 1) {
                break;
            }
            index--;

            // Store this parameter in local variable storage
            final int finalN = n;
            this.visitor.visitVarInsn(storeOpcode, finalN);
            if(LogUtils.LOGGING_ENABLED) {
                logger.info("Executing store: {}_{} for {}", storeOpcode, finalN, p);
            }

            // Create an entry to re-load from local storage
            loadStack.push(() -> {
                        if(LogUtils.LOGGING_ENABLED) {
                            logger.info("Executing load {}_{} for {}", loadOpcode, finalN, p);
                        }
                this.visitor.visitVarInsn(loadOpcode, finalN);
            });
            n += Type.getType(p).getSize();
        }

        // Load the parameters out of local storage
        while (!loadStack.isEmpty()) {
            Runnable l = loadStack.pop();
            l.run();
        }
    }

    public void modifyReturnType() {
        if(LogUtils.LOGGING_ENABLED) {
            logger.info("Calling return type transformation");
        }
        // Call the transformation callbacks
        if (this.needsReturnTransformation()) {
            for (ReturnTransformation t : this.returnTransformations) {
                t.transformReturnValue(this.visitor, this.descriptor);
            }
        }
    }


}
