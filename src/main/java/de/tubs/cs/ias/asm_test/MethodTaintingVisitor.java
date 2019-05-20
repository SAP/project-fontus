package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MethodTaintingVisitor extends MethodVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Some methods are not handled in a generic fashion, one can defined specialized proxies here
     */
    private final HashMap<FunctionCall, Runnable> methodProxies;
    /**
     * Some dynamic method invocations can't be handled generically. Add proxy functions here.
     */
    private final HashMap<de.tubs.cs.ias.asm_test.ProxiedDynamicFunctionEntry, Runnable> dynProxies;
    /**
     * Some StringBuilder methods require special handling, performed by a 1 to 1 mapping.
     */
    private final HashMap<String, String> stringBuilderMethodsToRename;
    /**
     * String like classes, need special handling
     */
    private final HashMap<String, MethodInvocation> stringClasses;
    /**
     * Pattern to replacement for field types
     */
    private final Collection<Map.Entry<Pattern, String>> fieldTypes;
    /**
     * All functions listed here return Strings that should be marked as tainted.
     */
    private final List<FunctionCall> sources;
    /**
     * All functions listed here consume Strings that need to be checked first.
     */
    private final List<FunctionCall> sinks;
    private int used, usedAfterInjection;

    MethodTaintingVisitor(int acc, String signature, MethodVisitor methodVisitor) {
        super(Opcodes.ASM7, methodVisitor);
        this.used = Type.getArgumentsAndReturnSizes(signature)>>2;
        if((acc&Opcodes.ACC_STATIC)!=0) this.used--; // no this

        this.methodProxies = new HashMap<>();
        this.dynProxies = new HashMap<>();
        this.stringBuilderMethodsToRename = new HashMap<>();
        this.sources = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.stringClasses = new HashMap<>();
        this.fieldTypes = new ArrayList<>();
        this.fillProxies();
        this.fillMethodsToRename();
        this.fillSources();
        this.fillSinks();
        this.rewriteOwnerMethods();
        this.fillFieldTypes();
    }

    /**
     * See https://stackoverflow.com/questions/47674972/getting-the-number-of-local-variables-in-a-method
     * for keeping track of used locals..
     */

    @Override
    public void visitFrame(
            int type, int numLocal, Object[] local, int numStack, Object[] stack) {
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
        int newMax = var + Utils.storeOpcodeSize(opcode);
        if (newMax > this.used) this.used = newMax;
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, Math.max(this.used, this.usedAfterInjection));
    }


    /**
     * Initialize the field types needing special handling here.
     */
    private void fillFieldTypes() {
        this.fieldTypes.add(Map.entry(Constants.strPattern, Constants.TStringDesc));
        this.fieldTypes.add(Map.entry(Constants.strBuilderPattern, Constants.TStringBuilderDesc));
    }

    /**
     * String like class names need special handling. Initialize the mapping here.
     */
    private void rewriteOwnerMethods() {
        this.stringClasses.put(Constants.StringBuilder, this::visitStringBuilderMethod);
        this.stringClasses.put(Constants.String, this::visitStringMethod);
    }

    /**
     * Calls to sinks, i.e., methods that always return String values.
     * Those shall be handled by marking the returned taint-aware String as tainted.
     */
    private void fillSinks() {
        this.sinks.add(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
    }

    /**
     * Calls to sources, i.e., methods that should not be called with Strings marked as tainted.
     */
    private void fillSources() {
        this.sources.add(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "next", Constants.ToStringDesc, false));
        this.sources.add(new FunctionCall(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextLine", Constants.ToStringDesc, false));

    }

    /**
     *  Initializes the methods that shall be renamed map.
     */
    private void fillMethodsToRename() {
        this.stringBuilderMethodsToRename.put("toString", "toIASString");
    }

    /**
     *  Initializes the method proxy maps.
     */
    private void fillProxies() {
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(" + Constants.TStringDesc + ";" + Constants.TStringDesc + ";)" + Constants.TStringDesc + ";", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;I)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(" + Constants.TStringDesc + ";I)" + Constants.TStringDesc + ";", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;J)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(" + Constants.TStringDesc + ";J)" + Constants.TStringDesc + ";", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;D)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(" + Constants.TStringDesc + ";D)" + Constants.TStringDesc + ";", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;F)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(" + Constants.TStringDesc + ";F)" + Constants.TStringDesc + ";", false));

        // All the Wrapper classes have some common methods performing String conversions, add Proxies in a generic fashion.
        Collection<Map.Entry<String, String>> numberTypes = new ArrayList<>();
        numberTypes.add(Map.entry("Byte", "B"));
        numberTypes.add(Map.entry("Short", "S"));
        numberTypes.add(Map.entry("Integer", "I"));
        numberTypes.add(Map.entry("Long", "J"));
        numberTypes.add(Map.entry("Double", "D"));
        numberTypes.add(Map.entry("Float", "F"));

        for(Map.Entry<String, String> e : numberTypes) {
          String owner = String.format("java/lang/%s", e.getKey());
          // TODO: UGLY HACK
          String parseName = String.format("parse%s", "Integer".equals(e.getKey()) ? "Int" : e.getKey());
          String parseDescriptor = String.format("(Ljava/lang/String;)%s", e.getValue());

          this.methodProxies.put(
                  new FunctionCall(Opcodes.INVOKESTATIC, owner, parseName, parseDescriptor, false),
                  () -> {
                      logger.info("Augmenting call to {}.{}{}", owner, parseName, parseDescriptor);
                      super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, Constants.TStringToStringName, Constants.ToStringDesc, false);
                      super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, parseName, parseDescriptor, false);
                  }
          );
          String toHexStringName = "toHexString";
          String toStringDesc = String.format("(%s)Ljava/lang/String;", e.getValue());
            this.methodProxies.put(
                    new FunctionCall(Opcodes.INVOKESTATIC, owner, toHexStringName, toStringDesc, false),
                    () -> {
                        logger.info("Augmenting call to {}.{}{}", owner, toHexStringName, toStringDesc);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, toHexStringName, toStringDesc, false);
                        this.stringToTString();
                    }
            );

            String toStringName = "toString";
            this.methodProxies.put(
                    new FunctionCall(Opcodes.INVOKESTATIC, owner, toStringName, toStringDesc, false),
                    () -> {
                        logger.info("Augmenting call to {}.{}{}", owner, toStringName, toStringDesc);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, toStringName, toStringDesc, false);
                        this.stringToTString();
                    }
            );
            String valueOfName = "valueOf";
            String valueOfDesc = String.format("(Ljava/lang/String;)L%s;", owner);
            this.methodProxies.put(
                    new FunctionCall(Opcodes.INVOKESTATIC, owner, valueOfName, valueOfDesc, false),
                    () -> {
                        logger.info("Augmenting call to {}:{}{}", owner, valueOfName, valueOfDesc);
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, Constants.TStringToStringName, Constants.ToStringDesc, false);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, valueOfName, valueOfDesc, false);
                    }
            );
        }
        this.methodProxies.put(
                new FunctionCall(Opcodes.INVOKESPECIAL, "java/util/Scanner", Constants.Init, "(Ljava/lang/String;)V", false),
                () -> {
                    logger.info("Augmenting call to java/util/Scanner.<init>(Ljava/lang/String;)V");
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, Constants.TStringToStringName, Constants.ToStringDesc, false);
                    super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", Constants.Init, "(Ljava/lang/String;)V" , false);
                }
        );
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
        super.visitTypeInsn(Opcodes.NEW, Constants.TString);
        super.visitInsn(Opcodes.DUP);
        super.visitInsn(Opcodes.DUP2_X1);
        super.visitInsn(Opcodes.POP2);
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TString, Constants.Init, Constants.TStringInitUntaintedDesc, false);
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
        if(this.sinks.contains(pfe)) {
            logger.info("{}.{}{} is a sinks, so calling the check taint function before passing the value!", owner, name, descriptor);
            // Call dup here to put the TString reference twice on the stack so the call can pop one without affecting further processing
            super.visitInsn(Opcodes.DUP);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, "abortIfTainted", "()V", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, Constants.TStringToStringName, Constants.ToStringDesc, false);
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
        if(this.sources.contains(pfe)) {
            logger.info("{}.{}{} is a source, so tainting String by calling {}.tainted!", owner, name, descriptor, Constants.TString);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "tainted", "(Ljava/lang/String;)" + Constants.TStringDesc + ";", false);
            return true;
        }
        return false;
    }

    /**
     * Replace access to fields of type IASString/IASStringBuilder
     */
    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {

        for(Map.Entry<Pattern, String> e : this.fieldTypes) {
            Pattern pattern = e.getKey();
            Matcher matcher = pattern.matcher(descriptor);
            if (matcher.find()) {
                String newDescriptor = matcher.replaceAll(e.getValue());
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
        if(this.isSinkCall(opcode, owner, name, descriptor, isInterface) || this.isSourceCall(opcode, owner, name, descriptor, isInterface)) {
            return;
        }

        // If a method has a defined proxy, apply it right away
        if (this.shouldBeProxied(opcode, owner, name, descriptor, isInterface)) {
            return;
        }

        // We have special methods to rewrite methods belonging to a specific owner, e.g. to String or StringBuilder
        if(this.stringClasses.containsKey(owner)) {
            MethodInvocation mi = this.stringClasses.get(owner);
            mi.invoke(opcode, owner, name, descriptor, isInterface);
            return;
        }

        boolean jdkMethod = owner.contains("java");

        // Don't rewrite IASString/IASStringBuilder functions
        boolean skipInvoke = jdkMethod || owner.contains(Constants.TString) || owner.contains(Constants.TStringBuilder);

        Matcher sbDescMatcher = Constants.strBuilderPattern.matcher(descriptor);
        Matcher stringDescMatcher = Constants.strPattern.matcher(descriptor);

        // JDK methods need special handling.
        // If there isn't a proxy defined, we will just convert taint-aware Strings to regular ones before calling the function and vice versa for the return value.
        if(jdkMethod && stringDescMatcher.find()) {
            this.handleJdkMethod(opcode, owner, name, descriptor, isInterface);
            return;
        }

        // toString method, make a taint-aware String of the result
        if("toString".equals(name) && descriptor.endsWith(")Ljava/lang/String;")) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            this.stringToTString();
            return;
        }

        // TODO: case when both find()s are true?
        if (stringDescMatcher.find() && !skipInvoke) {
            String newDescriptor = stringDescMatcher.replaceAll(Constants.TStringDesc);
            logger.info("Rewriting invoke containing String [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, owner, name, newDescriptor);
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

    private void handleJdkMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        Descriptor desc = Descriptor.parseDescriptor(descriptor);
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
            if ((Constants.StringDesc).equals(p)) {
                logger.info("Converting taint-aware String to String");
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, Constants.TStringToStringName, Constants.ToStringDesc, false);
            }

            final int finalN = n;
            super.visitVarInsn(storeOpcode, finalN);
            logger.info("Executing {}_{} for {}", storeOpcode, finalN, p);

            loadStack.push(() -> {
                //logger.info("Executing load {}_{}", loadOpcode, finalN);
                super.visitVarInsn(loadOpcode, finalN);
            });
            n += Utils.storeOpcodeSize(storeOpcode);
        }
        assert n == this.usedAfterInjection;
        while (!loadStack.empty()) {
            Runnable l = loadStack.pop();
            l.run();
        }

        logger.info("invoking [{}] {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if ((Constants.StringDesc).equals(desc.getReturnType())) {
            this.stringToTString();
        }
    }

    /**
     * The 'ldc' instruction loads a constant value out of the constant pool.
     *
     * It might load String values, so we have to transform them.
     */
    @Override
    public void visitLdcInsn(final Object value) {
        // When loading a constant, make a taint-aware string out of a string constant.
        if (value instanceof String) {
            logger.info("Rewriting String LDC to IASString LDC instruction");
            super.visitTypeInsn(Opcodes.NEW, Constants.TString);
            super.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(value);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TString, Constants.Init, Constants.TStringInitUntaintedDesc, false);
        } else {
            super.visitLdcInsn(value);
        }
    }

    /**
     * We want to override some instantiations of classes with our own types
     */
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        logger.info("Visiting type [{}] instruction: {}", type, opcode);
        switch (type) {
            case Constants.StringBuilder:
                super.visitTypeInsn(opcode, Constants.TStringBuilder);
                break;
            case Constants.String:
                super.visitTypeInsn(opcode, Constants.TString);
                break;
            default:
                super.visitTypeInsn(opcode, type);
        }
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

        if (this.shouldBeDynProxied(name, descriptor)) {
            return;
        }

        logger.info("invokeDynamic {}{}", name, descriptor);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);

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
        String newOwner = Constants.TString;
        String newDescriptor = stringDescMatcher.replaceAll(Constants.TStringDesc);
        logger.info("Rewriting String invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, name, newDescriptor);
        super.visitMethodInsn(opcode, newOwner, name, newDescriptor, isInterface);
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
        String newOwner = Constants.TStringBuilder;
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

}
