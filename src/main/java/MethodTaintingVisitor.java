import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MethodTaintingVisitor extends MethodVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Some methods are not handled in a generic fashion, one can defined specialized proxies here
     */
    private final HashMap<ProxiedFunctionEntry, Runnable> methodProxies;
    /**
     * Some dynamic method invocations can't be handled generically. Add proxy functions here.
     */
    private final HashMap<ProxiedDynamicFunctionEntry, Runnable> dynProxies;
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
    private final List<ProxiedFunctionEntry> sources;
    /**
     * All functions listed here consume Strings that need to be checked first.
     * TODO: Add handling of tainted values in some appropriate fashion.
     */
    private final List<ProxiedFunctionEntry> sinks;

    MethodTaintingVisitor(MethodVisitor methodVisitor) {
        super(Opcodes.ASM7, methodVisitor);
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
        this.sinks.add(new ProxiedFunctionEntry("java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
    }

    /**
     * Calls to sources, i.e., methods that should not be called with Strings marked as tainted.
     */
    private void fillSources() {
        this.sources.add(new ProxiedFunctionEntry("java/util/Scanner", "next", Constants.ToStringDesc));
        this.sources.add(new ProxiedFunctionEntry("java/util/Scanner", "nextLine", Constants.ToStringDesc));

    }
    /**
     *  Initializes the methods that shall be renamed map.
     */
    private void fillMethodsToRename() {
        // TODO: make dynamic!
        this.stringBuilderMethodsToRename.put("toString", "toIASString");
    }

    /**
     *  Initializes the method proxy maps.
     */
    private void fillProxies() {
        // TODO: make dynamic!
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;LIASString;)LIASString;", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;I)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;I)LIASString;", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;J)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;J)LIASString;", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;D)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;D)LIASString;", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;F)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;F)LIASString;", false));

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
                  new ProxiedFunctionEntry(owner, parseName, parseDescriptor),
                  () -> {
                      logger.info("Augmenting call to {}.{}{}", owner, parseName, parseDescriptor);
                      super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, Constants.TStringToStringName, Constants.ToStringDesc, false);
                      super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, parseName, parseDescriptor, false);
                  }
          );
          String toHexStringName = "toHexString";
          String toStringDesc = String.format("(%s)Ljava/lang/String;", e.getValue());
            this.methodProxies.put(
                    new ProxiedFunctionEntry(owner, toHexStringName, toStringDesc),
                    () -> {
                        logger.info("Augmenting call to {}.{}{}", owner, toHexStringName, toStringDesc);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, toHexStringName, toStringDesc, false);
                        this.stringToTString();
                    }
            );

            String toStringName = "toString";
            this.methodProxies.put(
                    new ProxiedFunctionEntry(owner, toStringName, toStringDesc),
                    () -> {
                        logger.info("Augmenting call to {}.{}{}", owner, toStringName, toStringDesc);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, toStringName, toStringDesc, false);
                        this.stringToTString();
                    }
            );
            String valueOfName = "valueOf";
            String valueOfDesc = String.format("(Ljava/lang/String;)L%s;", owner);
            this.methodProxies.put(
                    new ProxiedFunctionEntry(owner, valueOfName, valueOfDesc),
                    () -> {
                        logger.info("Augmenting call to {}:{}{}", owner, valueOfName, valueOfDesc);
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, Constants.TStringToStringName, Constants.ToStringDesc, false);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, valueOfName, valueOfDesc, false);
                    }
            );
        }
        this.methodProxies.put(
                new ProxiedFunctionEntry("java/util/Scanner", Constants.Init, "(Ljava/lang/String;)V"),
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
        ProxiedFunctionEntry pfe = new ProxiedFunctionEntry(owner, name, descriptor);
        if(this.sinks.contains(pfe)) {
            logger.info("{}.{}{} is a sinks, so calling the check taint function before passing the value!", owner, name, descriptor);
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
        ProxiedFunctionEntry pfe = new ProxiedFunctionEntry(owner, name, descriptor);
        if(this.sources.contains(pfe)) {
            logger.info("{}.{}{} is a source, so tainting String by calling {}.tainted!", owner, name, descriptor, Constants.TString);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "tainted", "(Ljava/lang/String;)LIASString;", false);
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
        if (this.shouldBeProxied(owner, name, descriptor)) {
            return;
        }

        // We have special methods to rewrite methods belonging to a specific owner, e.g. to String or StringBuilder
        if(this.stringClasses.containsKey(owner)) {
            MethodInvocation mi = this.stringClasses.get(owner);
            mi.invoke(opcode, owner, name, descriptor, isInterface);
            return;
        }

        // toString method, make a taint-aware String of the result
        if("toString".equals(name) && descriptor.endsWith(")Ljava/lang/String;")) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            this.stringToTString();
            return;
        }

        // Don't rewrite Java standard library functions or IASString/IASStringBuilder functions
        boolean skipInvoke = owner.contains("java") || owner.contains(Constants.TString) || owner.contains(Constants.TStringBuilder);

        Matcher sbDescMatcher = Constants.strBuilderPattern.matcher(descriptor);
        Matcher stringDescMatcher = Constants.strPattern.matcher(descriptor);
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

    /**
     * The 'ldc' instruction loads a constant value out of the constant pool.
     *
     * It might load String values, so we have to transform them.
     * TODO: Transforming compile time strings is not very nice from a Performance PoV, but unsure whether we can avoid it.
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
    private boolean shouldBeProxied(String owner, String name, String descriptor) {
        ProxiedFunctionEntry pfe = new ProxiedFunctionEntry(owner, name, descriptor);
        if (this.methodProxies.containsKey(pfe)) {
            logger.info("Proxying call to {}.{}{}", owner, name, descriptor);
            Runnable pf = this.methodProxies.get(pfe);
            pf.run();
            return true;
        }
        return false;
    }

}
