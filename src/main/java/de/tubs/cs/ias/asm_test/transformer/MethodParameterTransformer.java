package de.tubs.cs.ias.asm_test.transformer;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;
import de.tubs.cs.ias.asm_test.instrumentation.MethodTaintingVisitor;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MethodParameterTransformer {
    private static final ParentLogger logger = LogUtils.getLogger();

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
        return (Type.getArgumentsAndReturnSizes(this.descriptor.toDescriptor()) >> 2) - 1;
    }

    public boolean needsTransformation() {
        return !this.paramTransformations.isEmpty() || !this.returnTransformations.isEmpty();
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
     * local variable storage. Once each parameter is transformed (as necessary), the
     * stack is recreated from local variables.
     * <p>
     * Note the caller still needs to invoke the actual method!
     *
     * @param nUsedLocalVariables The number of local variables currently used by the
     * calling method.
     */
    public void modifyStackParameters(int nUsedLocalVariables) {

        if (this.paramTransformations.isEmpty()) {
            return;
        }

        Stack<Runnable> loadStack = new Stack<>();
        Stack<String> params = this.descriptor.getParameterStack();

        // The current local variable slot
        int n = nUsedLocalVariables;
        // The current method parameter, starting at the end (on top of the stack)
        int index = params.size() - 1;
        // With zero parameters, the loop is skipped
        while (!params.empty()) {
            String p = params.pop();
            int storeOpcode = Type.getType(p).getOpcode(Opcodes.ISTORE);
            int loadOpcode = Type.getType(p).getOpcode(Opcodes.ILOAD);

            // Call the transformation callbacks in reverse order!
            for (int i = this.paramTransformations.size() - 1; i >= 0; i--) {
                logger.info("Calling transformation: {} on parameter {} for {}", i, index, p);
                this.paramTransformations.get(i).transform(index, p, this.visitor);
            }

            // If there is just one parameter, can skip storing and loading stack
            if (this.descriptor.parameterCount() == 1) {
                break;
            }
            index--;

            // Store this parameter in local variable storage
            final int finalN = n;
            this.visitor.visitVarInsn(storeOpcode, finalN);
            logger.info("Executing store: {}_{} for {}", storeOpcode, finalN, p);

            // Create an entry to re-load from local storage
            loadStack.push(() -> {
                logger.info("Executing load {}_{} for {}", loadOpcode, finalN, p);
                this.visitor.visitVarInsn(loadOpcode, finalN);
            });
            n += Type.getType(p).getSize();
        }

        // Load the parameters out of local storage
        while (!loadStack.empty()) {
            Runnable l = loadStack.pop();
            l.run();
        }
    }

    public void modifyReturnType() {
        logger.info("Calling return type transformation");
        // Call the transformation callbacks
        for (ReturnTransformation t : this.returnTransformations) {
            t.transform(this.visitor, this.descriptor);
        }
    }


}
