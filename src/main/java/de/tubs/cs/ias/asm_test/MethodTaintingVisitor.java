package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class MethodTaintingVisitor extends MethodVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Pattern STRING_BUILDER_QN_PATTERN = Pattern.compile(Constants.StringBuilderQN, Pattern.LITERAL);
    private static final Pattern STRING_QN_PATTERN = Pattern.compile(Constants.StringQN, Pattern.LITERAL);
    private static final Pattern STRING_BUFFER_QN_PATTERN = Pattern.compile(Constants.StringBufferQN, Pattern.LITERAL);

    private boolean shouldRewriteCheckCast = false;
    private final String name;
    private final String methodDescriptor;
    /**
     * Some methods are not handled in a generic fashion, one can defined specialized proxies here
     */
    private final HashMap<FunctionCall, Runnable> methodProxies;
    /**
     * Some dynamic method invocations can't be handled generically. Add proxy functions here.
     */
    private final HashMap<ProxiedDynamicFunctionEntry, Runnable> dynProxies;
    /**
     * Some StringBuilder methods require special handling, performed by a 1 to 1 mapping.
     */
    private final HashMap<String, String> stringBuilderMethodsToRename;
    private final HashMap<String, String> stringMethodsToRename;
    private final HashMap<String, String> stringBufferMethodsToRename;

    /**
     * String like classes, need special handling
     */
    private final HashMap<String, MethodInvocation> stringClasses;
    /**
     * Pattern to replacement for field types
     */
    private final Collection<Tuple<Pattern, String>> fieldTypes;

    private final Configuration configuration = Configuration.instance;

    private int used;


    private int usedAfterInjection;

    MethodTaintingVisitor(int acc, String name, String methodDescriptor, MethodVisitor methodVisitor) {
        super(Opcodes.ASM7, methodVisitor);
        this.used = Type.getArgumentsAndReturnSizes(methodDescriptor) >> 2;
        if ((acc & Opcodes.ACC_STATIC) != 0) this.used--; // no this
        this.name = name;
        this.methodDescriptor = methodDescriptor;
        this.methodProxies = new HashMap<>();
        this.dynProxies = new HashMap<>();
        this.stringBuilderMethodsToRename = new HashMap<>();
        this.stringMethodsToRename = new HashMap<>();
        this.stringBufferMethodsToRename = new HashMap<>();
        this.stringClasses = new HashMap<>();
        this.fieldTypes = new ArrayList<>();
        this.fillProxies();
        this.fillMethodsToRename();
        this.rewriteOwnerMethods();
        this.fillFieldTypes();
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


    /**
     * Initialize the field types needing special handling here.
     */
    private void fillFieldTypes() {
        this.fieldTypes.add(Tuple.of(Constants.strPattern, Constants.TStringDesc));
        this.fieldTypes.add(Tuple.of(Constants.strBuilderPattern, Constants.TStringBuilderDesc));
        this.fieldTypes.add(Tuple.of(Constants.strBufferPattern, Constants.TStringBufferDesc));
    }

    /**
     * String like class names need special handling. Initialize the mapping here.
     */
    private void rewriteOwnerMethods() {
        this.stringClasses.put(Constants.StringBuilderQN, this::visitStringBuilderMethod);
        this.stringClasses.put(Constants.StringBufferQN, this::visitStringBufferMethod);
        this.stringClasses.put(Constants.StringQN, this::visitStringMethod);

    }

    /**
     * Initializes the methods that shall be renamed map.
     */
    private void fillMethodsToRename() {
        this.stringBuilderMethodsToRename.put(Constants.ToString, "toIASString");
        this.stringMethodsToRename.put(Constants.ToString, "toIASString");
        this.stringBufferMethodsToRename.put(Constants.ToString, "toIASString");

    }

    /**
     * Initializes the method proxy maps.
     */
    private void fillProxies() {
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ReflectionProxiesQN, "classForName", String.format("(%s)Ljava/lang/Class;", Constants.TStringDesc), false));
        this.methodProxies.put(new FunctionCall(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", false),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ReflectionProxiesQN, "classForName", String.format("(%sZLjava/lang/ClassLoader;)Ljava/lang/Class;", Constants.TStringDesc), false));

    }

    /**
     * Converts a String that's top of the stack to an taint-aware String
     * Precondition: String instance that's on top of the Stack!!
     */
    private void stringToTString() {
        /*
        Operand stack:
        +-------+ new  +----------+ dup  +----------+ dup2_x1  +----------+  pop2  +----------+ ispecial  +----------+
        |String +----->+IASString +----->+IASString +--------->+IASString +------->+String    +---------->+IASString |
        +-------+      +----------+      +----------+          +----------+        +----------+ init      +----------+
                       +----------+      +----------+          +----------+        +----------+
                       |String    |      |IASString |          |IASString |        |IASString |
                       +----------+      +----------+          +----------+        +----------+
                                         +----------+          +----------+        +----------+
                                         |String    |          |String    |        |IASString |
                                         +----------+          +----------+        +----------+
                                                               +----------+
                                                               |IASString |
                                                               +----------+
                                                               +----------+
                                                               |IASString |
                                                               +----------+
        */
        super.visitTypeInsn(Opcodes.NEW, Constants.TStringQN);
        super.visitInsn(Opcodes.DUP);
        super.visitInsn(Opcodes.DUP2_X1);
        super.visitInsn(Opcodes.POP2);
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringQN, Constants.Init, Constants.TStringInitUntaintedDesc, false);
    }

    private void stringBufferToTStringBuffer() {
        super.visitTypeInsn(Opcodes.NEW, Constants.TStringBufferQN);
        super.visitInsn(Opcodes.DUP);
        super.visitInsn(Opcodes.DUP2_X1);
        super.visitInsn(Opcodes.POP2);
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringBufferQN, Constants.Init, String.format("(%s)V", Constants.StringBufferDesc), false);
    }

    private void stringBuilderToTStringBuilder() {
        super.visitTypeInsn(Opcodes.NEW, Constants.TStringBuilderQN);
        super.visitInsn(Opcodes.DUP);
        super.visitInsn(Opcodes.DUP2_X1);
        super.visitInsn(Opcodes.POP2);
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringBuilderQN, Constants.Init, String.format("(%s)V", Constants.StringBuilderDesc), false);
    }
    /**
     * If a taint-aware string is on the top of the stack, we can call this function to add a check to handle tainted strings.
     */
    private void callCheckTaint() {
        super.visitInsn(Opcodes.DUP);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.ABORT_IF_TAINTED, "()V", false);
    }

    @Override
    public void visitInsn(int opcode) {
        this.shouldRewriteCheckCast = false;
        // If we are in a "toString" method, we have to insert a call to the taint-check before returning.
        if (opcode == Opcodes.ARETURN && Constants.ToStringDesc.equals(this.methodDescriptor) && Constants.ToString.equals(this.name)) {
            this.callCheckTaint();
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.TStringToStringName, Constants.ToStringDesc, false);
        }
        super.visitInsn(opcode);
    }

    /**
     * If the method is in the list of sinks: call the taint check before call it. Return true in this case.
     * Return false otherwise.
     */
    private boolean isSinkCall(final int opcode,
                               final String owner,
                               final String name,
                               final String descriptor,
                               final boolean isInterface) {
        FunctionCall pfe = new FunctionCall(opcode, owner, name, descriptor, isInterface);
        if (this.configuration.getSinks().contains(pfe)) {
            logger.info("{}.{}{} is a sink, so calling the check taint function before passing the value!", owner, name, descriptor);
            // Call dup here to put the TString reference twice on the stack so the call can pop one without affecting further processing
            this.callCheckTaint();
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.TStringToStringName, Constants.ToStringDesc, false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, isInterface);
            return true;
        }
        return false;
    }

    /**
     * If the method is in the list of sources: call it, mark the returned String as tainted. Return true in this case.
     * Return false otherwise.
     */
    private boolean isSourceCall(final int opcode,
                                 final String owner,
                                 final String name,
                                 final String descriptor,
                                 final boolean isInterface) {
        FunctionCall pfe = new FunctionCall(opcode, owner, name, descriptor, isInterface);
        if (this.configuration.getSources().contains(pfe)) {
            logger.info("{}.{}{} is a source, so tainting String by calling {}.tainted!", owner, name, descriptor, Constants.TStringQN);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringQN, "tainted", Constants.CreateTaintedStringDesc, false);
            return true;
        }
        return false;
    }

    /**
     * Replace access to fields of type IASString/IASStringBuilder
     */
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
        this.shouldRewriteCheckCast = false;

        for (Tuple<Pattern, String> e : this.fieldTypes) {
            Pattern pattern = e.x;
            Matcher matcher = pattern.matcher(descriptor);
            if (matcher.find()) {
                String newDescriptor = matcher.replaceAll(e.y);
                super.visitFieldInsn(opcode, owner, name, newDescriptor);
                return;
            }
        }

        super.visitFieldInsn(opcode, owner, name, descriptor);
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

        if (this.isSinkCall(opcode, owner, name, descriptor, isInterface) || this.isSourceCall(opcode, owner, name, descriptor, isInterface)) {
            return;
        }

        // If a method has a defined proxy, apply it right away
        if (this.shouldBeProxied(opcode, owner, name, descriptor, isInterface)) {
            return;
        }

        // We have special methods to rewrite methods belonging to a specific owner, e.g. to String or StringBuilder
        if (this.stringClasses.containsKey(owner)) {
            MethodInvocation mi = this.stringClasses.get(owner);
            mi.invoke(opcode, owner, name, descriptor, isInterface);
            return;
        }

        boolean jdkMethod = JdkClassesLookupTable.instance.isJdkClass(owner);

        // ToString wrapping
        if (!jdkMethod && name.equals(Constants.ToString) && descriptor.equals(Constants.ToStringDesc)) {
            super.visitMethodInsn(opcode, owner, Constants.ToStringInstrumented, Constants.ToStringInstrumentedDesc, isInterface);
            return;
        }

        // Don't rewrite IASString/IASStringBuilder functions
        boolean skipInvoke = jdkMethod || owner.contains(Constants.TStringQN) || owner.contains(Constants.TStringBuilderQN);

        Matcher sbDescMatcher = Constants.strBuilderPattern.matcher(descriptor);
        Matcher stringDescMatcher = Constants.strPattern.matcher(descriptor);
        Matcher stringBufferDescMatcher = Constants.strBufferPattern.matcher(descriptor);

        // JDK methods need special handling.
        // If there isn't a proxy defined, we will just convert taint-aware Strings to regular ones before calling the function and vice versa for the return value.
        if (jdkMethod) {
            this.handleJdkMethod(opcode, owner, name, descriptor, isInterface);
            return;
        }

        // TODO: case when both find()s are true?
        if (stringDescMatcher.find() && !skipInvoke) {
            String newDescriptor = stringDescMatcher.replaceAll(Constants.TStringDesc);
            logger.info("Rewriting invoke containing String [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, owner, name, newDescriptor);
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else if (stringBufferDescMatcher.find() && !skipInvoke) {
            String newDescriptor = stringBufferDescMatcher.replaceAll(Constants.TStringBufferDesc);
            logger.info("Rewriting invoke containing StringBuffer [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, owner, name, newDescriptor);
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else if (sbDescMatcher.find() && !skipInvoke) {
            String newDescriptor = sbDescMatcher.replaceAll(Constants.TStringBuilderDesc);
            logger.info("Rewriting invoke containing StringBuilder [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, owner, name, newDescriptor);
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else {
            logger.info("Skipping invoke [{}] {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    private static String rewriteDescriptor(String desc) {
        Matcher stringDescMatcher = Constants.strPattern.matcher(desc);
        String newDescriptor = desc;
        if (stringDescMatcher.find()) {
            newDescriptor = stringDescMatcher.replaceAll(Constants.TStringDesc);
        }
        return newDescriptor;
    }

    private void handleJdkMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        Descriptor desc = Descriptor.parseDescriptor(descriptor);
        switch (desc.parameterCount()) {
            case 0:
                break;
            case 1:
                this.handleSingleParameterJdkMethod(desc);
                break;
            default:
                this.handleMultiParameterJdkMethod(descriptor, desc);
                break;
        }
        logger.info("Invoking [{}] {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if (desc.hasStringLikeReturnType()) {
            logger.info("Converting returned String of {}.{}{}", owner, name, descriptor);
            this.stringToTString();
        } else if (desc.hasStringArrayReturnType()) {
            logger.info("Converting returned String Array of {}.{}{}", owner, name, descriptor);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringQN, "convertStringArray", String.format("(%s)%s", Constants.StringArrayDesc, Constants.TStringArrayDesc), false);
        } else if(desc.getReturnType().equals(Constants.StringBufferDesc)) {
            logger.info("Converting returned StringBuffer of {}.{}{}", owner, name, descriptor);
            this.stringBufferToTStringBuffer();
        } else if(desc.getReturnType().equals(Constants.StringBuilderDesc)) {
            logger.info("Converting returned StringBuilder of {}.{}{}", owner, name, descriptor);
            this.stringBuilderToTStringBuilder();
        }  else if(desc.getReturnType().equals(Constants.ObjectDesc)) {
            this.shouldRewriteCheckCast = true;
        }
    }

    private void handleSingleParameterJdkMethod(Descriptor desc) {
        if (!desc.hasStringLikeParameters()) return;

        String param = desc.getParameterStack().pop();
        if ((Constants.StringDesc).equals(param)) {
            logger.info("Converting taint-aware String to String in single param method invocation");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.TStringToStringName, Constants.ToStringDesc, false);
        }
    }

    private void handleMultiParameterJdkMethod(String descriptor, Descriptor desc) {
        if (!desc.hasStringLikeParameters()) return;

        // TODO: Add optimization that the upmost parameter on the stack does not need to be stored/loaded..
        Collection<String> parameters = desc.getParameters();
        Stack<Runnable> loadStack = new Stack<>();
        Stack<String> params = new Stack<>();
        params.addAll(parameters);
        int numVars = (Type.getArgumentsAndReturnSizes(descriptor) >> 2) - 1;
        this.usedAfterInjection = this.used + numVars;
        int n = this.used;
        while (!params.empty()) {
            String p = params.pop();
            int storeOpcode = Utils.getStoreOpcode(p);
            int loadOpcode = Utils.getLoadOpcode(p);

            //logger.info("Type: {}", p);
            if ((Constants.StringArrayDesc).equals(p)) {
                logger.info("Converting taint-aware String-Array to String-Array in multi param method invocation");
                super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringQN, "convertTaintAwareStringArray", String.format("(%s)%s", Constants.TStringArrayDesc, Constants.StringArrayDesc), false);
            }
            if ((Constants.StringDesc).equals(p)) {
                logger.info("Converting taint-aware String to String in multi param method invocation");
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.TStringToStringName, Constants.ToStringDesc, false);
            }

            final int finalN = n;
            super.visitVarInsn(storeOpcode, finalN);
            logger.info("Executing store: {}_{} for {}", storeOpcode, finalN, p);

            loadStack.push(() -> {
                logger.info("Executing load {}_{} for {}", loadOpcode, finalN, p);
                super.visitVarInsn(loadOpcode, finalN);
            });
            n += Utils.storeOpcodeSize(storeOpcode);
        }
        assert n == this.usedAfterInjection;
        while (!loadStack.empty()) {
            Runnable l = loadStack.pop();
            l.run();
        }

    }

    /**
     * The 'ldc' instruction loads a constant value out of the constant pool.
     * <p>
     * It might load String values, so we have to transform them.
     */
    @Override
    public void visitLdcInsn(final Object value) {
        this.shouldRewriteCheckCast = false;

        // When loading a constant, make a taint-aware string out of a string constant.
        if (value instanceof String) {
            logger.info("Rewriting String LDC to IASString LDC instruction");
            super.visitTypeInsn(Opcodes.NEW, Constants.TStringQN);
            super.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(value);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringQN, Constants.Init, Constants.TStringInitUntaintedDesc, false);
        } else if (value instanceof Type) {
            Type type = (Type) value;
            int sort = type.getSort();
            if (sort == Type.OBJECT) {
                if ("java.lang.String".equals(type.getClassName())) {
                    super.visitLdcInsn(Type.getObjectType(Constants.TStringQN));
                    return;
                } else if ("java.lang.StringBuilder".equals(type.getClassName())) {
                    super.visitLdcInsn(Type.getObjectType(Constants.TStringBuilderQN));
                    return;
                }
                //TODO: handle Arrays etc..
            }
            super.visitLdcInsn(value);
        } else {
            super.visitLdcInsn(value);
        }
    }

    /**
     * We want to override some instantiations of classes with our own types
     */
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        if(this.shouldRewriteCheckCast && opcode == Opcodes.CHECKCAST && Constants.StringQN.equals(type)) {
            logger.info("Rewriting checkcast to call to TString.fromObject(Object obj)");
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringUtilsQN, "fromObject", String.format("(%s)%s", Constants.ObjectDesc, Constants.TStringDesc), false);
            this.shouldRewriteCheckCast = false;
            return;
        }
        logger.info("Visiting type [{}] instruction: {}", type, opcode);
        String newType = type;
        boolean isArray = type.charAt(0) == '[';
        if (type.equals(Constants.StringBufferQN) || (isArray && type.endsWith(Constants.StringBufferDesc))) {
            newType = STRING_BUFFER_QN_PATTERN.matcher(type).replaceAll(Matcher.quoteReplacement(Constants.TStringBufferQN));
        } else if (type.equals(Constants.StringBuilderQN) || (isArray && type.endsWith(Constants.StringBuilderDesc))) {
            newType = STRING_BUILDER_QN_PATTERN.matcher(type).replaceAll(Matcher.quoteReplacement(Constants.TStringBuilderQN));
        } else if (type.equals(Constants.StringQN)  || (isArray && type.endsWith(Constants.StringDesc))) {
            newType = STRING_QN_PATTERN.matcher(type).replaceAll(Matcher.quoteReplacement(Constants.TStringQN));
        }
        this.shouldRewriteCheckCast = false;
        super.visitTypeInsn(opcode, newType);
    }

    private void invokeConversionFunction(String type) {
        Map<String, String> types = new HashMap<>();
        types.put("I", "Integer");
        types.put("B", "Byte");
        types.put("C", "Character");
        types.put("D", "Double");
        types.put("F", "Float");
        types.put("J", "Long");
        types.put("S", "Short");
        types.put("Z", "Boolean");

        // No primitive type, nop
        if (!types.containsKey(type)) return;

        String full = types.get(type);
        String owner = String.format("java/lang/%s", full);
        String desc = String.format("(%s)L%s;", type, owner);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, "valueOf", desc, false);
    }

    /**
     * Pushes an integer onto the stack.
     * Optimizes small integers towards their dedicated ICONST_n instructions to save space.
     */
    private void pushNumberOnTheStack(int num) {
        switch (num) {
            case -1:
                super.visitInsn(Opcodes.ICONST_M1);
                return;
            case 0:
                super.visitInsn(Opcodes.ICONST_0);
                return;
            case 1:
                super.visitInsn(Opcodes.ICONST_1);
                return;
            case 2:
                super.visitInsn(Opcodes.ICONST_2);
                return;
            case 3:
                super.visitInsn(Opcodes.ICONST_3);
                return;
            case 4:
                super.visitInsn(Opcodes.ICONST_4);
                return;
            case 5:
                super.visitInsn(Opcodes.ICONST_5);
                return;
            default:
                super.visitIntInsn(Opcodes.BIPUSH, num);
        }
    }

    /**
     * Translates the call to a lambda function
     */
    private void invokeVisitLambdaCall(final String name,
                                       final String descriptor,
                                       final Handle bootstrapMethodHandle,
                                       final Object... bootstrapMethodArguments) {
        Object[] bsArgs = new Object[bootstrapMethodArguments.length];
        for (int i = 0; i < bootstrapMethodArguments.length; i++) {
            Object arg = bootstrapMethodArguments[i];
            if (arg instanceof Handle) {
                Handle a = (Handle) arg;
                bsArgs[i] = Utils.instrumentHandle(a);
            } else if (arg instanceof Type) {
                Type a = (Type) arg;
                bsArgs[i] = Utils.instrumentType(a);
            } else {
                bsArgs[i] = arg;
            }
        }
        String desc = rewriteDescriptor(descriptor);
        super.visitInvokeDynamicInsn(name, desc, bootstrapMethodHandle, bsArgs);
    }

    /**
     * We might have to proxy these as they do some fancy String concat optimization stuff.
     */
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
            this.invokeVisitLambdaCall(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
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
        this.pushNumberOnTheStack(parameterCount);
        super.visitTypeInsn(Opcodes.ANEWARRAY, Constants.ObjectQN);
        int currRegister = this.used;
        super.visitVarInsn(Opcodes.ASTORE, currRegister);
        // newly created array is now stored in currRegister, concat operands on top
        Stack<String> parameters = desc.getParameterStack();
        int paramIndex = 0;
        while (!parameters.empty()) {
            String parameter = parameters.pop();
            // Convert topmost value (if required)
            this.invokeConversionFunction(parameter);
            // put array back on top
            super.visitVarInsn(Opcodes.ALOAD, currRegister);
            // swap array and object to array
            super.visitInsn(Opcodes.SWAP);
            // push the index where the value shall be stored
            this.pushNumberOnTheStack(paramIndex);
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
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringQN, "concat", Constants.ConcatDesc, false);
    }

    /**
     * Visit a method belonging to java/lang/String
     */
    private void visitStringMethod(final int opcode,
                                   final String owner,
                                   final String name,
                                   final String descriptor,
                                   final boolean isInterface) {
        Matcher stringDescMatcher = Constants.strPattern.matcher(descriptor);
        String newOwner = Constants.TStringQN;
        String newDescriptor = stringDescMatcher.replaceAll(Constants.TStringDesc);
        // TODO: this call is superfluous, TString.toTString is a NOP pretty much.. Maybe drop those calls?
        String newName = this.stringMethodsToRename.getOrDefault(name, name);
        logger.info("Rewriting String invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, newDescriptor);
        super.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
    }

    /**
     * Visit a method belonging to java/lang/StringBuffer
     */
    private void visitStringBufferMethod(final int opcode,
                                         final String owner,
                                         final String name,
                                         final String descriptor,
                                         final boolean isInterface) {
        Matcher sbDescMatcher = Constants.strBufferPattern.matcher(descriptor);
        String newOwner = Constants.TStringBufferQN;
        String newDescriptor = sbDescMatcher.replaceAll(Constants.TStringBufferDesc);
        // Replace all instances of java/lang/String
        Matcher newDescriptorMatcher = Constants.strPattern.matcher(newDescriptor);
        String finalDescriptor = newDescriptorMatcher.replaceAll(Constants.TStringDesc);
        // Some methods names (e.g., toString) need to be replaced to not break things, look those up
        String newName = this.stringBufferMethodsToRename.getOrDefault(name, name);

        logger.info("Rewriting StringBuffer invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, finalDescriptor);
        super.visitMethodInsn(opcode, newOwner, newName, finalDescriptor, isInterface);
    }

    /**
     * Visit a method belonging to java/lang/StringBuilder
     */
    private void visitStringBuilderMethod(final int opcode,
                                          final String owner,
                                          final String name,
                                          final String descriptor,
                                          final boolean isInterface) {
        Matcher sbDescMatcher = Constants.strBuilderPattern.matcher(descriptor);
        String newOwner = Constants.TStringBuilderQN;
        String newDescriptor = sbDescMatcher.replaceAll(Constants.TStringBuilderDesc);
        // Replace all instances of java/lang/String
        Matcher newDescriptorMatcher = Constants.strPattern.matcher(newDescriptor);
        String finalDescriptor = newDescriptorMatcher.replaceAll(Constants.TStringDesc);
        // Some methods names (e.g., toString) need to be replaced to not break things, look those up
        String newName = this.stringBuilderMethodsToRename.getOrDefault(name, name);

        logger.info("Rewriting StringBuilder invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, finalDescriptor);
        super.visitMethodInsn(opcode, newOwner, newName, finalDescriptor, isInterface);
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
    private boolean shouldBeProxied(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        FunctionCall pfe = new FunctionCall(opcode, owner, name, descriptor, isInterface);
        if (this.methodProxies.containsKey(pfe)) {
            logger.info("Proxying call to {}.{}{}", owner, name, descriptor);
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
