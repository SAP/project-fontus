package de.tubs.cs.ias.asm_test.asm;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.FunctionCall;
import de.tubs.cs.ias.asm_test.MethodTaintingUtils;
import de.tubs.cs.ias.asm_test.MethodTaintingVisitor;
import de.tubs.cs.ias.asm_test.Utils;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodParameterTransformer {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public interface ParameterTransformation {

        /**
         * Called for each parameter in a method.
         * 
         * The implementation should decide whether a transformation needs to take
         * place, and if so, can implement it as necessary. The caller should ensure
         * that the stack is left with the same number of entries as before, otherwise
         * the storage will fail.
         * 
         * @param index   The parameter index being transformed
         * @param p       The type descriptor of the parameter
         * @param visitor The method visitor
         */
        public void ParameterTransformation(int i, String type, MethodTaintingVisitor visitor);
    }

    public interface ReturnTransformation {
        public void ReturnTransformation(MethodTaintingVisitor visitor, Descriptor desc);
    }

    public MethodParameterTransformer(MethodTaintingVisitor visitor, FunctionCall function) {
        this.visitor = visitor;
        this.function = function;
        paramTransformations = new ArrayList<>();
        returnTransformations = new ArrayList<>();
    }

    public void AddParameterTransformation(ParameterTransformation transformation) {
        if (transformation != null) {
            paramTransformations.add(transformation);
        }
    }

    public void AddReturnTransformation(ReturnTransformation transformation) {
        if (transformation != null) {
            returnTransformations.add(transformation);
        }
    }

    private Descriptor getFunctionDescriptor() {
        return Descriptor.parseDescriptor(this.function.getDescriptor());
    }

    public boolean rewriteCheckCast() {
        return getFunctionDescriptor().getReturnType().equals(Constants.ObjectDesc);
    }

    public int getExtraStackSlots() {
        return (Type.getArgumentsAndReturnSizes(getFunctionDescriptor().toDescriptor()) >> 2) - 1;
    }

    public boolean needsTransformation() {
        return !paramTransformations.isEmpty() || !returnTransformations.isEmpty();
    }

    /**
     * Walks through method parameters on the stack, calling Listener.TransformParameter
     * for each parameter. Can be used to transform or check parameters before a method
     * is called.
     *  
     * Should be called before an invoke method bytecode operation, and assumes the
     * stack is populated with method parameters.
     * 
     * The callback is made on each parameter, before moving the stack varable into
     * local variable storage. Once each parameter is transformed (as necessary), the
     * stack is recreated from local variables.
     * 
     * Note the caller still needs to invoke the actual method!
     * 
     * @param call The method which is about to be called
     * @param nUsedLocalVariables The number of local variables currently used by the
     * calling method.
     * @return The total number of local variables required by the caller
     */
    public void ModifyStackParameters(int nUsedLocalVariables) {

        if (this.paramTransformations.isEmpty()) {
            return;
        }
    
        Stack<Runnable> loadStack = new Stack<>();
        Stack<String> params = getFunctionDescriptor().getParameterStack();

        // The current local variable slot
        int n = nUsedLocalVariables;
        // The current method parameter, starting at the end (on top of the stack)
        int index = params.size() - 1;
	// With zero parameters, the loop is skipped
        while (!params.empty()) {
            String p = params.pop();
            int storeOpcode = Utils.getStoreOpcode(p);
            int loadOpcode = Utils.getLoadOpcode(p);

            // Call the transformation callbacks in reverse order!
            for (int i = this.paramTransformations.size(); i-- > 0;) {
                logger.info("Calling transformation: {} on parameter {} for {}", i, index, p);
                this.paramTransformations.get(i).ParameterTransformation(index, p, this.visitor);
            }

	    // If there is just one parameter, can skip storing and loading stack
	    if (getFunctionDescriptor().parameterCount() == 1) {
                break;
            }
            index--;

            // Store this parameter in local variable storage
            final int finalN = n;
            visitor.visitVarInsn(storeOpcode, finalN);
            logger.info("Executing store: {}_{} for {}", storeOpcode, finalN, p);

            // Create an entry to re-load from local storage
            loadStack.push(() -> {
                logger.info("Executing load {}_{} for {}", loadOpcode, finalN, p);
                visitor.visitVarInsn(loadOpcode, finalN);
            });
            n += Utils.storeOpcodeSize(storeOpcode);
        }

        // Load the parameters out of local storage
        while (!loadStack.empty()) {
            Runnable l = loadStack.pop();
            l.run();
        }
    }

    public void ModifyReturnType() {
    	logger.info("Calling return type transformation");
        // Call the transformation callbacks
        for (ReturnTransformation t : this.returnTransformations) {
            t.ReturnTransformation(this.visitor, this.getFunctionDescriptor());
        }
    }

   private MethodTaintingVisitor visitor;
   private final FunctionCall function;

   private List<ParameterTransformation> paramTransformations;
   private List<ReturnTransformation> returnTransformations;

}
