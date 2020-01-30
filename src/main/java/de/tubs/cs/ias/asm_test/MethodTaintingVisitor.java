package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.Sink;
import de.tubs.cs.ias.asm_test.config.SinkParameter;
import de.tubs.cs.ias.asm_test.asm.BasicMethodVisitor;
import de.tubs.cs.ias.asm_test.asm.MethodParameterTransformer;
import de.tubs.cs.ias.asm_test.strategies.method.*;
import de.tubs.cs.ias.asm_test.strategies.InstrumentationHelper;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;


@SuppressWarnings("deprecation")
public class MethodTaintingVisitor extends BasicMethodVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private boolean shouldRewriteCheckCast;
    private final String name;
    private final String methodDescriptor;
    private final ClassResolver resolver;
    /**
     * Some methods are not handled in a generic fashion, one can defined specialized proxies here
     */
    private final HashMap<FunctionCall, Runnable> methodProxies;
    /**
     * Some dynamic method invocations can't be handled generically. Add proxy functions here.
     */
    private final HashMap<ProxiedDynamicFunctionEntry, Runnable> dynProxies;

    private int used;
    private int usedAfterInjection;

    private final Collection<MethodInstrumentationStrategy> instrumentation = new ArrayList<>(4);

    private final Configuration config;

    MethodTaintingVisitor(int acc, String name, String methodDescriptor, MethodVisitor methodVisitor, ClassResolver resolver, Configuration config) {
        super(Opcodes.ASM7, methodVisitor);
        this.resolver = resolver;
        logger.info("Instrumenting method: {}{}", name, methodDescriptor);
        this.used = Type.getArgumentsAndReturnSizes(methodDescriptor) >> 2;
        this.usedAfterInjection = 0;
        if ((acc & Opcodes.ACC_STATIC) != 0) this.used--; // no this
        this.shouldRewriteCheckCast = false;
        this.name = name;
        this.methodDescriptor = methodDescriptor;
        this.methodProxies = new HashMap<>();
        this.dynProxies = new HashMap<>();
        this.fillProxies();
        this.instrumentation.add(new StringMethodInstrumentationStrategy(this.getParentVisitor()));
        this.instrumentation.add(new StringBuilderMethodInstrumentationStrategy(this.getParentVisitor()));
        this.instrumentation.add(new StringBufferMethodInstrumentationStrategy(this.getParentVisitor()));
        this.instrumentation.add(new DefaultMethodInstrumentationStrategy(this.getParentVisitor()));
	    this.config = config;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        this.shouldRewriteCheckCast = false;
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    /**
     * See https://stackoverflow.com/questions/47674972/getting-the-number-of-local-variables-in-a-method
     * for keeping track of used locals..
     */

    @Override
    public void visitFrame(
            int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        this.shouldRewriteCheckCast = false;
        if (type != Opcodes.F_NEW)
            throw new IllegalStateException("only expanded frames supported");
        int l = numLocal;
        for (int ix = 0; ix < numLocal; ix++)
            if (local[ix] == Opcodes.LONG || local[ix] == Opcodes.DOUBLE) l++;
        if (l > this.used) this.used = l;
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        this.shouldRewriteCheckCast = false;
        int newMax = var + Utils.storeOpcodeSize(opcode);
        if (newMax > this.used) this.used = newMax;
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        this.shouldRewriteCheckCast = false;
        super.visitMaxs(maxStack, Math.max(this.used, this.usedAfterInjection));
    }

    private void visitMethodInsn(FunctionCall fc) {
        super.visitMethodInsn(fc.getOpcode(), fc.getOwner(), fc.getName(), fc.getDescriptor(), fc.isInterface());
    }

    /**
     * Initializes the method proxy maps.
     */
    private void fillProxies() {
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringUtilsQN, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ReflectionProxiesQN, "classForName", String.format("(%s)Ljava/lang/Class;", Constants.TStringDesc), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ReflectionProxiesQN, "classForName", String.format("(%sZLjava/lang/ClassLoader;)Ljava/lang/Class;", Constants.TStringDesc), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLEncoder", "encode", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, String.format("%sTURLEncoder", Constants.TPackage), "encode", String.format("(%s%s)%s", Constants.TStringDesc, Constants.TStringDesc, Constants.TStringDesc), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLEncoder", "encode", "(Ljava/lang/String;)Ljava/lang/String;", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, String.format("%sTURLEncoder", Constants.TPackage), "encode", String.format("(%s)%s", Constants.TStringDesc, Constants.TStringDesc), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLDecoder", "decode", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, String.format("%sTURLDecoder", Constants.TPackage), "decode", String.format("(%s%s)%s", Constants.TStringDesc, Constants.TStringDesc, Constants.TStringDesc), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/net/URLDecoder", "decode", "(Ljava/lang/String;)Ljava/lang/String;", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, String.format("%sTURLDecoder", Constants.TPackage), "decode", String.format("(%s)%s", Constants.TStringDesc, Constants.TStringDesc), false));
    }

    @Override
    public void visitInsn(int opcode) {
        this.shouldRewriteCheckCast = false;
        // If we are in a "toString" method, we have to insert a call to the taint-check before returning.
        if (opcode == Opcodes.ARETURN && Constants.ToStringDesc.equals(this.methodDescriptor) && Constants.ToString.equals(this.name)) {
            MethodTaintingUtils.callCheckTaint(this.getParentVisitor());
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.TStringToStringName, Constants.ToStringDesc, false);
        }
        super.visitInsn(opcode);
    }

    /**
     * Replace access to fields of type IASString/IASStringBuilder
     */
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
        this.shouldRewriteCheckCast = false;

        for (MethodInstrumentationStrategy s : this.instrumentation) {
            if (s.instrumentFieldIns(opcode, owner, name, descriptor)) {
                return;
            }
        }
    }

    /**
     * All method calls are handled here.
     */
    @Override
    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
        this.shouldRewriteCheckCast = false;
        FunctionCall fc = new FunctionCall(opcode, owner, name, descriptor, isInterface);

//        if (this.isSinkCall(fc) || this.isSourceCall(fc)) {
//            return;
//        }

        // If a method has a defined proxy, apply it right away
        if (this.shouldBeProxied(fc)) {
            return;
        }

        for (MethodInstrumentationStrategy s : this.instrumentation) {
            if (s.rewriteOwnerMethod(opcode, owner, name, descriptor, isInterface)) {
                return;
            }
        }

        // Call any functions which manipulate function call parameters and return types
        // for example sources, sinks and JDK functions
        if (this.rewriteParametersAndReturnType(fc)) {
            return;
        }

        // ToString wrapping
        if (name.equals(Constants.ToString) && descriptor.equals(Constants.ToStringDesc)) {
            super.visitMethodInsn(opcode, owner, Constants.ToStringInstrumented, Constants.ToStringInstrumentedDesc, isInterface);
            return;
        }

        Descriptor desc = Descriptor.parseDescriptor(descriptor);
        for (MethodInstrumentationStrategy s : this.instrumentation) {
            desc = s.instrument(desc);
        }

        if (desc.toDescriptor().equals(descriptor)) {
            logger.info("Skipping invoke [{}] {}.{}{}", Utils.opcodeToString(opcode), owner, name, desc.toDescriptor());
        } else {
            logger.info("Rewriting invoke containing String-like type [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, owner, name, desc.toDescriptor());
        }
        super.visitMethodInsn(opcode, owner, name, desc.toDescriptor(), isInterface);
    }


    private class JdkMethodTransformer implements MethodParameterTransformer.Transformation {

        private FunctionCall call;

        public JdkMethodTransformer(FunctionCall call) {
            this.call = call;
        }

        @Override
        public void ParameterTransformation(int index, String type, MethodTaintingVisitor visitor) {

            for (MethodInstrumentationStrategy s : visitor.instrumentation) {
                s.insertJdkMethodParameterConversion(type);
            }

            FunctionCall converter = config.getConverterForParameter(this.call, index);
            if(converter != null) {
                visitor.visitMethodInsn(converter);
            }
        }

        @Override
        public void ReturnTransformation(MethodTaintingVisitor visitor, Descriptor desc) {

            for (MethodInstrumentationStrategy s : visitor.instrumentation) {
                s.instrumentReturnType(call.getOwner(), call.getName(), desc);
            }
    
            FunctionCall converter = config.getConverterForReturnValue(call);
            if(converter != null) {
                visitor.visitMethodInsn(converter);
            }

        }

    }

    private class SourceSinkTransformer implements MethodParameterTransformer.Transformation {

        private FunctionCall call;

        public SourceSinkTransformer(FunctionCall call) {
            this.call = call;
        }

        public boolean isSourceOrSink() {
            return (this.getSink() != null);
        }
        private Sink getSink() {
            return config.getSinkConfig().getSinkForFunction(call);
        }
 
        @Override
        public void ParameterTransformation(int index, String type, MethodTaintingVisitor visitor) {

            if (getSink() == null) {
                return;
            }

            // Sink checks
            logger.debug("Type: {}", type);
            // Check whether this parameter needs to be checked for taint
            SinkParameter sp = getSink().findParameter(index);
            if (sp != null) {
                if (InstrumentationHelper.canHandleType(type)) {
                    logger.info("Adding taint check for sink {}, paramater {} ({})", getSink().getName(), index, type);
                    MethodTaintingUtils.callCheckTaint(visitor.getParent());
                } else {
                    logger.warn("Tried to check taint for type {} (index {}) in sink {} although it is not taintable!", type, index, getSink().getName());
                }
            }
        }

        @Override
        public void ReturnTransformation(MethodTaintingVisitor visitor, Descriptor desc) {

           // Source transforms go here!
           // super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringQN, "tainted", Constants.CreateTaintedStringDesc, false);
        }

    }

    private boolean rewriteParametersAndReturnType(FunctionCall call) {
        
        MethodParameterTransformer transformer = new MethodParameterTransformer(this, call);

        // Add JDK transformations
        Descriptor desc = Descriptor.parseDescriptor(call.getDescriptor());
        if (desc.hasStringLikeParameters() || config.needsParameterConversion(call)) {
            transformer.AddTransformation(new JdkMethodTransformer(call));
        }

        // Add Source and Sink transformations
        SourceSinkTransformer sourceSinkTransform = new SourceSinkTransformer(call);
        if (sourceSinkTransform.isSourceOrSink()) {
            transformer.AddTransformation(sourceSinkTransform);
        }

        // No transformations required
        if (!transformer.needsTransformation()) {
            return false;
        }

        // Do the transformations
        transformer.ModifyStackParameters(this.used);
        this.usedAfterInjection = Math.max(this.usedAfterInjection, transformer.getExtraStackSlots());
        // Make the call
        logger.info("Invoking [{}] {}.{}{}", Utils.opcodeToString(call.getOpcode()), call.getOwner(), call.getName(), call.getDescriptor());
        this.visitMethodInsn(call);
        // Modify Return parameters
        transformer.ModifyReturnType();
        this.shouldRewriteCheckCast = transformer.rewriteCheckCast();

        return true;
    }

    /**
     * The 'ldc' instruction loads a constant value out of the constant pool.
     * <p>
     * It might load String values, so we have to transform them.
     */
    @Override
    public void visitLdcInsn(final Object value) {
        this.shouldRewriteCheckCast = false;

        for (MethodInstrumentationStrategy s : this.instrumentation) {
            if (s.handleLdc(value)) {
                return;
            }
        }

        if (value instanceof Type) {
            Type type = (Type) value;
            int sort = type.getSort();
            if (sort == Type.OBJECT) {
                for (MethodInstrumentationStrategy s : this.instrumentation) {
                    if (s.handleLdcType(type)) {
                        return;
                    }
                }
                //TODO: handle Arrays etc..
            } else if (sort == Type.ARRAY) {
                for (MethodInstrumentationStrategy s : this.instrumentation) {
                    if (s.handleLdcArray(type)) {
                        return;
                    }
                }
            }
        }
        super.visitLdcInsn(value);
    }


    /**
     * We want to override some instantiations of classes with our own types
     */
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        if (this.shouldRewriteCheckCast && opcode == Opcodes.CHECKCAST && Constants.StringQN.equals(type)) {
            logger.info("Rewriting checkcast to call to TString.fromObject(Object obj)");
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringUtilsQN, "fromObject", String.format("(%s)%s", Constants.ObjectDesc, Constants.TStringDesc), false);
            this.shouldRewriteCheckCast = false;
            return;
        }
        logger.info("Visiting type [{}] instruction: {}", type, opcode);
        String newType = type;
        for (MethodInstrumentationStrategy s : this.instrumentation) {
            newType = s.rewriteTypeIns(newType);
        }
        this.shouldRewriteCheckCast = false;
        super.visitTypeInsn(opcode, newType);
    }

    @Override
    public void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments) {
        this.shouldRewriteCheckCast = false;

        if (this.shouldBeDynProxied(name, descriptor)) {
            return;
        }

        if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethodHandle.getOwner()) &&
                "metafactory".equals(bootstrapMethodHandle.getName())) {
            MethodTaintingUtils.invokeVisitLambdaCall(this.getParentVisitor(), name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            return;
        }

        if ("makeConcatWithConstants".equals(name)) {
            this.rewriteConcatWithConstants(name, descriptor, bootstrapMethodArguments);
            return;
        }

        logger.info("invokeDynamic {}{}", name, descriptor);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    private void rewriteConcatWithConstants(String name, String descriptor, Object[] bootstrapMethodArguments) {
        logger.info("Trying to rewrite invokeDynamic {}{} towards Concat!", name, descriptor);

        Descriptor desc = Descriptor.parseDescriptor(descriptor);
        assert bootstrapMethodArguments.length == 1;
        Object fmtStringObj = bootstrapMethodArguments[0];
        assert fmtStringObj instanceof String;
        String formatString = (String) fmtStringObj;
        int parameterCount = desc.parameterCount();
        MethodTaintingUtils.pushNumberOnTheStack(this.getParentVisitor(), parameterCount);
        super.visitTypeInsn(Opcodes.ANEWARRAY, Constants.ObjectQN);
        int currRegister = this.used;
        super.visitVarInsn(Opcodes.ASTORE, currRegister);
        // newly created array is now stored in currRegister, concat operands on top
        Stack<String> parameters = desc.getParameterStack();
        int paramIndex = 0;
        while (!parameters.empty()) {
            String parameter = parameters.pop();
            // Convert topmost value (if required)
            MethodTaintingUtils.invokeConversionFunction(this.getParentVisitor(), parameter);
            // put array back on top
            super.visitVarInsn(Opcodes.ALOAD, currRegister);
            // swap array and object to array
            super.visitInsn(Opcodes.SWAP);
            // push the index where the value shall be stored
            MethodTaintingUtils.pushNumberOnTheStack(this.getParentVisitor(), paramIndex);
            // swap, this puts them into the order arrayref, index, value
            super.visitInsn(Opcodes.SWAP);
            // store the value into arrayref at index, next parameter is on top now (if there are any more)
            super.visitInsn(Opcodes.AASTORE);
            paramIndex++;
        }

        // Load the format String constant
        super.visitLdcInsn(formatString);
        // Load the param array
        super.visitVarInsn(Opcodes.ALOAD, currRegister);
        // Call our concat method
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringUtilsQN, "concat", Constants.ConcatDesc, false);
    }

    /**
     * Is there a dynamic proxy defined? If so apply and return true.
     */
    private boolean shouldBeDynProxied(String name, String descriptor) {
        ProxiedDynamicFunctionEntry pdfe = new ProxiedDynamicFunctionEntry(name, descriptor);
        if (this.dynProxies.containsKey(pdfe)) {
            logger.info("Proxying dynamic call to {}{}", name, descriptor);
            Runnable pf = this.dynProxies.get(pdfe);
            pf.run();
            return true;
        }
        return false;
    }

    /**
     * Is there a proxy defined? If so apply and return true.
     */
    private boolean shouldBeProxied(FunctionCall pfe) {
        if (this.methodProxies.containsKey(pfe)) {
            logger.info("Proxying call to {}.{}{}", pfe.getOwner(), pfe.getName(), pfe.getDescriptor());
            Runnable pf = this.methodProxies.get(pfe);
            pf.run();
            return true;
        }
        return false;
    }

    @Override
    public void visitCode() {
        this.shouldRewriteCheckCast = false;
        super.visitCode();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        this.shouldRewriteCheckCast = false;
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        this.shouldRewriteCheckCast = false;
        super.visitMethodInsn(opcode, owner, name, descriptor);
    }


    @Override
    public void visitJumpInsn(int opcode, Label label) {
        this.shouldRewriteCheckCast = false;
        super.visitJumpInsn(opcode, label);
    }


    @Override
    public void visitLabel(Label label) {
        this.shouldRewriteCheckCast = false;
        super.visitLabel(label);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        this.shouldRewriteCheckCast = false;
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        this.shouldRewriteCheckCast = false;
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        this.shouldRewriteCheckCast = false;
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        this.shouldRewriteCheckCast = false;
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.shouldRewriteCheckCast = false;
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.shouldRewriteCheckCast = false;
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.shouldRewriteCheckCast = false;
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        this.shouldRewriteCheckCast = false;
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.shouldRewriteCheckCast = false;
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitEnd() {
        this.shouldRewriteCheckCast = false;
        super.visitEnd();
    }
}
