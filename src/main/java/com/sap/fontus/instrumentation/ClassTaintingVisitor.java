package com.sap.fontus.instrumentation;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.*;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.*;
import com.sap.fontus.utils.lookups.AnnotationLookup;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;


class ClassTaintingVisitor extends ClassVisitor {
    private static final Logger logger = LogUtils.getLogger();

    private final List<BlackListEntry> blacklist = new ArrayList<>();
    /**
     * This blacklist contains the name of the inheriting class (not the jdk class).
     * The value is a list of corresponding blacklist entries which will be ignored for the generation of not overridden jdk methods
     */
    private final String newMainDescriptor;
    private final List<FieldData> staticFinalFields;
    private final ClassLoader loader;
    private final boolean containsJSRRET;
    private boolean hasClInit = false;
    private boolean isAnnotation = false;
    private boolean implementsInvocationHandler;
    private MethodVisitRecording recording;
    private final ClassVisitor visitor;
    private final Configuration config;
    private final ClassResolver resolver;
    /**
     * The name of the class currently processed.
     */
    private String owner;
    private String superName;
    private Set<Method> jdkMethods;
    private final Set<Method> overriddenJdkMethods;
    private String[] interfaces;
    private boolean isInterface;
    private boolean isFinal;
    private final CombinedExcludedLookup combinedExcludedLookup;
    private final SignatureInstrumenter signatureInstrumenter;
    private final List<org.objectweb.asm.commons.Method> instrumentedMethods = new ArrayList<>();
    private boolean inEnd;
    private boolean extendsJdkSuperClass;
    private final List<DynamicCall> bootstrapMethods = new ArrayList<>();
    /**
     * Uninstrumented methods which are used as lambdas of JDK or excluded functions
     * They will be proxied to their instrumented counterpart.
     */
    private final List<LambdaCall> jdkLambdaMethodProxies = new ArrayList<>();
    private final InstrumentationHelper instrumentationHelper;

    public ClassTaintingVisitor(ClassVisitor cv, ClassResolver resolver, Configuration config, ClassLoader loader, boolean containsJSRRET, CombinedExcludedLookup excludedLookup) {
        super(Opcodes.ASM9, cv);
        this.visitor = cv;
        this.staticFinalFields = new ArrayList<>();
        this.overriddenJdkMethods = new HashSet<>();
        this.resolver = resolver;
        this.loader = loader;
        this.config = config;
        this.containsJSRRET = containsJSRRET;
        this.instrumentationHelper = new InstrumentationHelper();
        this.newMainDescriptor = "(" + Type.getDescriptor(IASString[].class) + ")V";
        this.fillBlacklist();
        this.signatureInstrumenter = new SignatureInstrumenter(this.api, this.instrumentationHelper);
        this.combinedExcludedLookup = excludedLookup;
    }

    private void fillBlacklist() {
        //this.blacklist.add(new BlackListEntry("main", Constants.MAIN_METHOD_DESC, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC));
//        this.blacklist.add(new BlackListEntry(Constants.ToString, Constants.ToStringDesc, Opcodes.ACC_PUBLIC));
    }

    /**
     * To add the wrapped main method we need to record the class name we currently are operating on.
     */
    @Override
    public void visit(
            int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        this.owner = name;
        this.superName = superName == null ? Type.getInternalName(Object.class) : this.instrumentationHelper.instrumentSuperClass(superName);
        this.interfaces = interfaces;

        this.isInterface = ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
        this.isFinal =((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL);

        // Is this class/interface an annotation or annotation proxy class? If yes, don't instrument it
        // Cf Java Language Specification 12 - 9.6.1 Annotation Types
        if (AnnotationLookup.getInstance().isAnnotation(name, superName, interfaces, this.resolver)) {
            logger.info("{} is annotation or annotation proxy!", name);
            this.isAnnotation = true;
            AnnotationLookup.getInstance().addAnnotation(name);
        }

        this.implementsInvocationHandler = this.implementsInvocationHandler();
        this.extendsJdkSuperClass = this.combinedExcludedLookup.isPackageExcludedOrJdk(superName);

        // Getting JDK methods
        this.initJdkClasses();

        String instrumentedSignature = this.signatureInstrumenter.instrumentSignature(signature);

        // In order to correctly proxy the "toString" method, we add a default implementation
        // to interfaces which calls the instrumented toString. This is only allowed for classes
        // compiled with Java V8, so we need to increase the format version in some cases.
        //
        // Beware though, that reducing the version means that some UTF8 strings are interpreted
        // differently, which might lead to java.lang.ClassFormatError exceptions
        //
        if ((version < Opcodes.V1_8) && !this.isAnnotation && this.isInterface) {
             version = Opcodes.V1_8;
        }

        super.visit(version, access, name, instrumentedSignature, this.superName, interfaces);
    }

    private void initJdkClasses() {
        ClassTraverser classTraverser = new ClassTraverser(this.combinedExcludedLookup);
        if (!this.isAnnotation) {
            if (this.extendsJdkSuperClass) {
                classTraverser.readAllJdkMethods(this.superName, this.resolver);
            }
            classTraverser.addNotContainedJdkInterfaceMethods(this.superName, this.interfaces, this.resolver, this.loader);
        }
        this.jdkMethods = Collections.unmodifiableSet(classTraverser.getMethods());
    }

    /**
     * Replaces String like attributes with their taint-aware counterparts.
     */
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor,
                                   String signature, Object value) {
        if (this.isAnnotation) {
            return super.visitField(access, name, descriptor, signature, value);
        }

        return this.instrumentationHelper.instrumentFieldInstruction(this.visitor, access, name, descriptor, signature, value, (n, d, v) -> staticFinalFields.add(FieldData.of(n, d, v))).orElse(null);
    }


    /**
     * Checks whether the method is the 'clinit' method.
     */
    private static boolean isClInit(int access, String name, String desc) {
        return access == Opcodes.ACC_STATIC && Constants.ClInit.equals(name) && "()V".equals(desc);
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        if (this.isAnnotation) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        String instrumentedSignature = this.signatureInstrumenter.instrumentSignature(signature);
        MethodVisitor mv;
        Method method = new Method(access, owner, name, descriptor, signature, exceptions, this.isInterface);
        String desc = descriptor;
        String newName = name;

        if (this.recording == null && isClInit(access, name, desc) && !this.inEnd) {
            logger.info("Recording static initializer");
            RecordingMethodVisitor rmv = new RecordingMethodVisitor();
            this.recording = rmv.getRecording();
            this.hasClInit = true;
            return rmv;
        }
        // Create a new main method, wrapping the regular one and translating all Strings to IASStrings
        // TODO: acceptable for main is a parameter of String[] or String...! Those have different access bits set (i.e., the ACC_VARARGS bits are set too) -> Handle this nicer..
        if (((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) && (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC && "main".equals(name) && descriptor.equals(Constants.MAIN_METHOD_DESC)
                && !this.config.isClassMainBlacklisted(this.owner)) {
            logger.info("Creating proxy main method");
            MethodVisitor v = super.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", Constants.MAIN_METHOD_DESC, null, exceptions);
            this.createMainWrapperMethod(v);
            logger.info("Processing renamed main method.");
            mv = super.visitMethod(access, Constants.MainWrapper, this.newMainDescriptor, signature, exceptions);
            newName = Constants.MainWrapper;
            desc = this.newMainDescriptor;
        } else if (!this.isAnnotation && overridesJdkSuperMethod(method) && shouldBeInstrumented(descriptor)) {
            int newAccess = access & ~Opcodes.ACC_ABSTRACT;
            MethodVisitor v = super.visitMethod(newAccess, name, descriptor, signature, exceptions);

            this.overriddenJdkMethods.add(overriddenJdkSuperMethod(method));

            this.generateProxyToInstrumented(v, newName, Descriptor.parseDescriptor(descriptor), null, Optional.empty());

            desc = this.instrumentationHelper.instrumentForNormalCall(descriptor);
            mv = super.visitMethod(access, newName, desc, instrumentedSignature, exceptions);
        } else if (this.blacklist.contains(new BlackListEntry(name, descriptor, access))) {
            mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        } else if (this.instrumentedMethods.contains(new org.objectweb.asm.commons.Method(name, this.instrumentationHelper.instrumentForNormalCall(descriptor)))) {
            if (this.instrumentationHelper.instrumentForNormalCall(descriptor).equals(descriptor)) {
                // If a not instrumented method has been instrumented and afterwards the same method with a instrumented descriptor occurs, it has to be ignored
                return null;
            }
            int newAccess = access & ~Opcodes.ACC_ABSTRACT;
            MethodVisitor v = super.visitMethod(newAccess, name, descriptor, signature, exceptions);

            this.overriddenJdkMethods.add(overriddenJdkSuperMethod(method));

            this.generateProxyToInstrumented(v, newName, Descriptor.parseDescriptor(descriptor), null, Optional.empty());
            return null;
        } else {
            desc = this.instrumentationHelper.instrumentForNormalCall(descriptor);
            if (!desc.equals(descriptor)) {
                logger.info("Rewriting method signature {}{} to {}{}", name, descriptor, name, desc);
            }
            this.instrumentedMethods.add(new org.objectweb.asm.commons.Method(name, desc));
            mv = super.visitMethod(access, name, desc, instrumentedSignature, exceptions);
        }

        MethodTaintingVisitor mtv = new MethodTaintingVisitor(access, this.owner, newName, desc, mv, this.resolver, this.config, this.implementsInvocationHandler, this.instrumentationHelper, this.combinedExcludedLookup, this.bootstrapMethods, this.jdkLambdaMethodProxies, this.superName, this.loader, this.isInterface);
        if (this.containsJSRRET) {
            return new JSRInlinerAdapter(mtv, access, newName, desc, instrumentedSignature, exceptions);
        }
        return mtv;
    }

    private void generateProxyToInstrumented(MethodVisitor mv, String instrumentedName, Descriptor originalDescriptor, Descriptor instrumentedDescriptor, Optional<LambdaCall> lambdaCall) {
        if (instrumentedDescriptor == null) {
            instrumentedDescriptor = this.instrumentationHelper.instrument(originalDescriptor);
        }

        mv.visitCode();

        if (lambdaCall.isPresent() && lambdaCall.get().isConstructorCall()) {
            mv.visitTypeInsn(Opcodes.NEW, lambdaCall.get().getImplementation().getOwner());
            mv.visitInsn(Opcodes.DUP);
        }

        // TODO Handle lists
        int register = 0;

        if (!lambdaCall.isPresent() || (!lambdaCall.get().isStaticCall() && !lambdaCall.get().isConstructorCall())) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            register++;
        }

        int diff = originalDescriptor.parameterCount() - instrumentedDescriptor.parameterCount();
        for (int i = 0; i < instrumentedDescriptor.parameterCount(); i++) {
            String param = originalDescriptor.getParameters().get(i + diff);
            mv.visitVarInsn(loadCodeByType(param), register);
            if (isDescriptorNameToInstrument(param) && !param.equals(instrumentedDescriptor.getParameters().get(i))) {
                Type instrumentedType = Type.getType(this.instrumentationHelper.instrumentQN(param));

                if (Type.getType(param).equals(Type.getType(String.class))) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
                } else {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, instrumentedType.getInternalName());
                }
            }
            register += Type.getType(param).getSize();
        }

        int opcode;

        if (lambdaCall.isPresent()) {
            opcode = lambdaCall.get().getImplementation().getOpcode();
        } else {
            if ("<init>".equals(instrumentedName)) {
                opcode = Opcodes.INVOKESPECIAL;
            } else {
                opcode = Opcodes.INVOKEVIRTUAL;
            }
        }

        String owner;
        if (lambdaCall.isPresent()) {
            owner = lambdaCall.get().getImplementation().getOwner();
        } else {
            owner = this.owner;
        }

        mv.visitMethodInsn(opcode, owner, instrumentedName, instrumentedDescriptor.toDescriptor(), opcode == Opcodes.INVOKEINTERFACE);
        if (isDescriptorNameToInstrument(originalDescriptor.getReturnType())) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToOrigName, Constants.ConversionUtilsToOrigDesc, false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(originalDescriptor.getReturnType()).getInternalName());
        }

        int returnCode;
        if (lambdaCall.isPresent() && lambdaCall.get().isConstructorCall()) {
            returnCode = returnCodeByReturnType(Type.getObjectType(lambdaCall.get().getImplementation().getOwner()).getDescriptor());
        } else {
            returnCode = returnCodeByReturnType(originalDescriptor.getReturnType());
        }
        mv.visitInsn(returnCode);
        mv.visitMaxs(originalDescriptor.parameterCount() + 1, originalDescriptor.parameterCount() + 1);
        mv.visitEnd();
    }

    private int calculateDescArrayDimensions(String descriptor) {
        int counter = 0;
        for (int i = descriptor.indexOf('['); i >= 0; i = descriptor.indexOf('[', i + 1)) {
            counter++;
        }
        return counter;
    }

    private int loadCodeByType(String type) {
        return Type.getType(type).getOpcode(Opcodes.ILOAD);
    }

    private int returnCodeByReturnType(String returnTypeDescriptor) {
        return Type.getType(returnTypeDescriptor).getOpcode(Opcodes.IRETURN);
    }

    private boolean isDescriptorNameToInstrument(String qn) {
        return this.instrumentationHelper.handlesType(qn);
    }

    /**
     * Writes the code for a static initialization block.
     *
     * @param mv The MethodVisitor for the static initialization block. Should be a Taint-aware MethodVisitor!
     */
    private void createStaticStringInitializer(MethodVisitor mv) {
        logger.info("Creating a static initializer to initialize all static final String fields");
        mv.visitCode();
        Utils.writeToStaticInitializer(mv, this.owner, this.staticFinalFields);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();
    }

    @Override
    public void visitEnd() {
        this.inEnd = true;
        if (!this.isAnnotation) {
            logger.info("Overriding not overridden JDK methods which have to be instrumented");
            if (!this.isInterface) {
                this.overrideMissingJdkMethods();
            } else {
                this.declareMissingJdkMethods();
            }
        }
        if (!this.hasClInit && !this.staticFinalFields.isEmpty()) {
            logger.info("Adding a new static initializer to initialize static final String fields");
            MethodVisitor mv = this.visitMethod(Opcodes.ACC_STATIC, Constants.ClInit, "()V", null, null);
            this.createStaticStringInitializer(mv);
        } else if (this.hasClInit) {
            logger.info("Replaying static initializer and augmenting it");
            MethodVisitor mv = this.visitMethod(Opcodes.ACC_STATIC, Constants.ClInit, "()V", null, null);
            ClassInitializerAugmentingVisitor augmentingVisitor = new ClassInitializerAugmentingVisitor(mv, this.owner, this.staticFinalFields);
            this.recording.replay(augmentingVisitor);
        }

        if (!this.bootstrapMethods.isEmpty()) {
            this.generateBootstrapMethods();
        }

        if (!this.jdkLambdaMethodProxies.isEmpty()) {
            this.generateLambdaProxiesForJdk();
        }
        super.visitEnd();
    }

    private void generateLambdaProxiesForJdk() {
        List<LambdaCall> distinct = new ArrayList<>();

        for (LambdaCall f : this.jdkLambdaMethodProxies) {
            if (!distinct.contains(f)) {
                distinct.add(f);
            }
        }

        distinct.forEach(this::createLambdaProxyForJdk);
    }

    private void createLambdaProxyForJdk(LambdaCall call) {
        int access = Opcodes.ACC_PUBLIC;
        access |= Opcodes.ACC_STATIC;

        Descriptor proxyDescriptor = call.getProxyDescriptor(this.loader, this.instrumentationHelper);

        MethodVisitor mv = super.visitMethod(access, call.getProxyMethodName(), proxyDescriptor.toDescriptor(), null, null);

        if (this.combinedExcludedLookup.isPackageExcludedOrJdk(call.getImplementationHandle().getOwner())) {
            Descriptor uninstrumentedDescriptor = Descriptor.parseDescriptor(this.instrumentationHelper.uninstrument(call.getImplementation().getDescriptor()));
            this.generateProxyToJdk(mv, call.getImplementation().getName(), proxyDescriptor, uninstrumentedDescriptor, call);
        } else {
            Descriptor instrumentedDescriptor = this.instrumentationHelper.instrument(call.getImplementation().getParsedDescriptor());
            this.generateProxyToInstrumented(mv, call.getImplementation().getName(), proxyDescriptor, instrumentedDescriptor, Optional.of(call));
        }

    }

    private void generateProxyToJdk(MethodVisitor mv, String name, Descriptor proxyDescriptor, Descriptor uninstrumentedDescriptor, LambdaCall lambdaCall) {
        Objects.requireNonNull(mv);
        Objects.requireNonNull(name);
        Objects.requireNonNull(proxyDescriptor);
        Objects.requireNonNull(uninstrumentedDescriptor);
        Objects.requireNonNull(lambdaCall);

        mv.visitCode();

        if (lambdaCall.isConstructorCall()) {
            mv.visitTypeInsn(Opcodes.NEW, lambdaCall.getImplementation().getOwner());
            mv.visitInsn(Opcodes.DUP);
        }

        // TODO Handle lists
        int register = 0;

        if (!lambdaCall.isStaticCall() && !lambdaCall.isConstructorCall()) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            register++;
        }

        int diff = proxyDescriptor.parameterCount() - uninstrumentedDescriptor.parameterCount();
        for (int i = 0; i < uninstrumentedDescriptor.parameterCount(); i++) {
            String param = proxyDescriptor.getParameters().get(i + diff);
            mv.visitVarInsn(loadCodeByType(param), register);
            if (this.instrumentationHelper.isInstrumented(param) && !param.equals(uninstrumentedDescriptor.getParameters().get(i))) {
                Type uninstrumentedType = Type.getType(this.instrumentationHelper.uninstrument(param));

                if (uninstrumentedType.equals(Type.getType(String.class))) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(IASString.class), Constants.ToString, new Descriptor(Type.getType(String.class)).toDescriptor(), false);
                } else {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToOrigName, Constants.ConversionUtilsToOrigDesc, false);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, uninstrumentedType.getInternalName());
                }
            }
            register += Type.getType(param).getSize();
        }

        int opcode = lambdaCall.getImplementation().getOpcode();

        String owner = lambdaCall.getImplementation().getOwner();

        mv.visitMethodInsn(opcode, owner, name, uninstrumentedDescriptor.toDescriptor(), lambdaCall.isCallOnInterface());
        if (this.instrumentationHelper.isInstrumented(proxyDescriptor.getReturnType())) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(proxyDescriptor.getReturnType()).getInternalName());
        }

        int returnCode;
        if (lambdaCall.isConstructorCall()) {
            returnCode = returnCodeByReturnType(Type.getObjectType(lambdaCall.getImplementation().getOwner()).getDescriptor());
        } else {
            returnCode = returnCodeByReturnType(proxyDescriptor.getReturnType());
        }
        mv.visitInsn(returnCode);
        mv.visitMaxs(proxyDescriptor.parameterCount() + 1, proxyDescriptor.parameterCount() + 1);
        mv.visitEnd();
    }

    private void generateBootstrapMethods() {
        List<DynamicCall> filtered = new ArrayList<>();

        // TODO: Refactor this piece of code to get rid of the continue <label>
        outer:
        for (DynamicCall dynamicCall : this.bootstrapMethods) {
            for (DynamicCall existing : filtered) {
                if (dynamicCall.getOriginal().equals(existing.getOriginal())) {
                    continue outer;
                }
            }
            filtered.add(dynamicCall);
        }

        filtered.forEach(this::createBootstrapMethod);
    }

    private void declareMissingJdkMethods() {
        List<Method> methods = this.jdkMethods
                .stream()
                .filter(method -> !containsOverriddenJdkMethod(method))
                .filter(method -> shouldBeInstrumented(method.getDescriptor()))
                .filter(method -> !Modifier.isStatic(method.getAccess()))
                .filter(method -> !MethodUtils.isToString(method.getName(), method.getDescriptor()))
                .collect(Collectors.toList());
        methods.forEach(this::createJdkDeclaring);
    }

    private void createJdkDeclaring(Method method) {
        String signature = method.getSignature();
//        if (signature == null && ClassUtils.hasGenericInformation(method)) {
//            signature = Descriptor.getSignature(method);
//            signature = this.instrumentDescriptorStringlike(signature);
//        }
        int access = method.getAccess();
        String name = method.getName();
        String descriptor = this.instrumentationHelper.instrumentForNormalCall(method.getDescriptor());
        String[] exceptions = method.getExceptions();
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (method.isDefault()) {
            this.generateInstrumentedProxyToSuper(mv, method, Descriptor.parseDescriptor(method.getDescriptor()), Descriptor.parseDescriptor(descriptor));
        }
    }

    private boolean containsOverriddenJdkMethod(Method method) {
        return this.overriddenJdkMethods.stream().anyMatch(m -> m.equalsNameAndDescriptor(method));
    }

    private void overrideMissingJdkMethods() {
        Object comparator;
        List<Method> methods = this.jdkMethods
                .stream()
                .filter(method -> !containsOverriddenJdkMethod(method))
                .filter(method -> shouldBeInstrumented(method.getDescriptor()))
                .filter(method -> !Modifier.isStatic(method.getAccess()))
                .collect(Collectors.toList());
        methods.forEach(this::createInstrumentedJdkProxy);
    }

    private void createInstrumentedJdkProxy(Method m) {
        if (Configuration.getConfiguration().getJdkInheritanceBlacklist().containsKey(this.owner)) {
            List<BlackListEntry> blackList = Configuration.getConfiguration().getJdkInheritanceBlacklist().get(this.owner);
            for (BlackListEntry entry : blackList) {
                int accessFlag = (m.getAccess() & Modifier.PUBLIC) | (m.getAccess() & Modifier.PROTECTED) | (m.getAccess() & Modifier.PRIVATE);
                if (entry.matches(m.getName(), m.getDescriptor(), accessFlag)) {
                    return;
                }
            }
        }
        logger.info("Creating proxy for inherited, but not overridden JDK method " + m);
        Descriptor originalDescriptor = Descriptor.parseDescriptor(m.getDescriptor());
        Descriptor instrumentedDescriptor = this.instrumentationHelper.instrument(originalDescriptor);

        if (this.instrumentedMethods.contains(new org.objectweb.asm.commons.Method(m.getName(), instrumentedDescriptor.toDescriptor()))) {
            return;
        }

        String[] exceptions = m.getExceptions();
        String signature = m.getSignature();
//        if (ClassUtils.hasGenericInformation(m)) {
//            signature = Descriptor.getSignature(m);
//            signature = this.instrumentDescriptorStringlike(signature);
//        }
        // Generating proxy with instrumented descriptor
        int modifiers = (m.getAccess() & ~Modifier.ABSTRACT);
        MethodVisitor mv = super.visitMethod(modifiers, m.getName(), instrumentedDescriptor.toDescriptor(), signature, exceptions);
        if (this.extendsJdkSuperClass) {
            // If this class extends a JDK class (ie we could not add an instrumented method to the superclass),
            // then create a proxy with instrumented arguments to the non-instrumented super class.
            this.generateInstrumentedProxyToSuper(mv, m, originalDescriptor, instrumentedDescriptor);
        } else {
            // If the super class is not a JDK class, then an instrumented method will exist in the superclass,
            // which we can call as an instrumented method here
            this.generateInstrumentedProxyToInstrumentedSuper(mv, m, instrumentedDescriptor);
        }

        // Overriding method with original descriptor

        if (!Modifier.isFinal(m.getAccess())) {
            MethodVisitor mv2 = super.visitMethod(modifiers, m.getName(), originalDescriptor.toDescriptor(), signature, exceptions);
            // Create an uninstrumented proxy to the instrumented method in this class
            this.generateProxyToInstrumented(mv2, m.getName(), originalDescriptor, null, Optional.empty());
        }
    }

    private void createBootstrapMethod(DynamicCall dynamicCall) {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_STATIC, dynamicCall.getProxy().getName(), dynamicCall.getProxy().getDesc(), null, null);

        Descriptor instrumentedOriginalDescriptor = Descriptor.parseDescriptor(dynamicCall.getOriginal().getDesc());
        Descriptor proxyDescriptor = Descriptor.parseDescriptor(dynamicCall.getProxy().getDesc());

        mv.visitCode();

        for (int i = 0; i < instrumentedOriginalDescriptor.getParameters().size(); i++) {
            String instrOrigParam = instrumentedOriginalDescriptor.getParameters().get(i);
            String proxyParam = proxyDescriptor.getParameters().get(i);

            mv.visitVarInsn(loadCodeByType(instrOrigParam), i);
            if (!instrOrigParam.equals(proxyParam)) {
                // Only strings are possible as tainted type, therefore it must always be a string conversion
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
            }
        }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, dynamicCall.getOriginal().getOwner(), dynamicCall.getOriginal().getName(), dynamicCall.getOriginal().getDesc(), false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void generateInstrumentedProxyToInstrumentedSuper(MethodVisitor mv, Method m, Descriptor instrumentedDescriptor) {
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        // Load Parameters, but do not convert them in this case.Tr
        for (int i = 0; i < instrumentedDescriptor.parameterCount(); ) {
            String param = instrumentedDescriptor.getParameters().get(i);
            // Creating new Object if necessary and duplicating it for initialization
            mv.visitVarInsn(loadCodeByType(param), i + 1);
            i += Type.getType(param).getSize();
        }
        // Calling the actual method
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.superName, m.getName(), instrumentedDescriptor.toDescriptor(), false);
        mv.visitInsn(returnCodeByReturnType(instrumentedDescriptor.getReturnType()));
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void generateInstrumentedProxyToSuper(MethodVisitor mv, Method m, Descriptor origDescriptor, Descriptor instrumentedDescriptor) {
        // TODO Handle lists
        mv.visitCode();
        // Converting parameters
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        for (int i = 0; i < origDescriptor.parameterCount(); ) {
            String param = origDescriptor.getParameters().get(i);
            // Creating new Object if necessary and duplicating it for initialization
            if (isDescriptorNameToInstrument(param)) {
                mv.visitVarInsn(loadCodeByType(param), i + 1);
                Type instrumentedParam = Type.getType(this.instrumentationHelper.instrumentQN(param));
                Type origParam = Type.getType(param);
                int arrayDimensions = calculateDescArrayDimensions(param);
                mv.visitInsn(Opcodes.DUP);
                Label label = new Label();
                mv.visitJumpInsn(Opcodes.IFNULL, label);
                if (arrayDimensions == 0) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, instrumentedParam.getInternalName());
                    this.instrumentationHelper.insertJdkMethodParameterConversion(mv, origParam);
//                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, instrumentedParam.getInternalName(), this.getToOriginalMethod(param), new Descriptor(new String[]{}, param).toDescriptor(), false);
                } else {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToOrigName, Constants.ConversionUtilsToOrigDesc, false);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, origParam.getInternalName());
                }
                mv.visitLabel(label);
                mv.visitTypeInsn(Opcodes.CHECKCAST, origParam.getInternalName());
            } else {
                mv.visitVarInsn(loadCodeByType(param), i + 1);
            }
            i += Type.getType(param).getSize();
        }

        // Calling the actual method
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.superName, m.getName(), origDescriptor.toDescriptor(), false);

        // Converting the return type back
        if (isDescriptorNameToInstrument(origDescriptor.getReturnType())) {
            Type returnType = Type.getType(this.instrumentationHelper.instrumentQN(origDescriptor.getReturnType()));

            int arrayDimensions = calculateDescArrayDimensions(returnType.getInternalName());
            if (arrayDimensions == 0) {
                int resultLocalAddress = origDescriptor.parameterCount() + 1;
                Label label1 = new Label();
                Label label2 = new Label();
                mv.visitInsn(Opcodes.DUP);
                mv.visitJumpInsn(Opcodes.IFNULL, label1);

                mv.visitVarInsn(Opcodes.ASTORE, resultLocalAddress); // this, params, free storage => 0 indexed
                mv.visitTypeInsn(Opcodes.NEW, returnType.getInternalName());
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, resultLocalAddress);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, returnType.getInternalName(), Constants.Init, new Descriptor(new String[]{origDescriptor.getReturnType()}, "V").toDescriptor(), false);

                mv.visitJumpInsn(Opcodes.GOTO, label2);
                mv.visitLabel(label1);

                mv.visitInsn(Opcodes.POP);
                mv.visitInsn(Opcodes.ACONST_NULL);

                mv.visitLabel(label2);
            } else {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, returnType.getInternalName());
            }
        }

        mv.visitInsn(returnCodeByReturnType(instrumentedDescriptor.getReturnType()));
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    /**
     * Replaces the original main method's body with a wrapper.
     * It translates the String parameter array to a taint-aware String array and calls the original main method's code.
     * <p>
     * Autogenerated by asmify!
     */
    @SuppressWarnings("OverlyLongMethod")
    private void createMainWrapperMethod(MethodVisitor mv) {
        mv.visitCode();

        // Setup configuration if offline instrumentation
        if (config.isOfflineInstrumentation()) {
            org.objectweb.asm.commons.Method parseOffline;
            try {
                parseOffline = org.objectweb.asm.commons.Method.getMethod(Configuration.class.getMethod("parseOffline", TaintMethod.class));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            String parseOfflineOwner = Utils.dotToSlash(Configuration.class.getName());
            String parseOfflineName = parseOffline.getName();
            String parseOfflineDescriptor = parseOffline.getDescriptor();
            String taintMethodOwner = Utils.dotToSlash(TaintMethod.class.getName());
            String taintMethodName = Configuration.getConfiguration().getTaintMethod().name();
            String taintMethodDescriptor = Descriptor.classNameToDescriptorName(taintMethodOwner);


            mv.visitFieldInsn(Opcodes.GETSTATIC, taintMethodOwner, taintMethodName, taintMethodDescriptor);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, parseOfflineOwner, parseOfflineName, parseOfflineDescriptor, false);
        }

        Label label0 = new Label();
        mv.visitLabel(label0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(IASString.class));
        mv.visitVarInsn(Opcodes.ASTORE, 1);
        Label label1 = new Label();
        mv.visitLabel(label1);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, 2);
        Label label2 = new Label();
        mv.visitLabel(label2);
        mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Type.getDescriptor(IASString[].class), Opcodes.INTEGER}, 0, null);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        Label label3 = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPGE, label3);
        Label label4 = new Label();
        mv.visitLabel(label4);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(IASString.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(IASString.class), Constants.Init, Constants.TStringInitUntaintedDesc, false);
        mv.visitInsn(Opcodes.AASTORE);
        Label label5 = new Label();
        mv.visitLabel(label5);
        mv.visitIincInsn(2, 1);
        mv.visitJumpInsn(Opcodes.GOTO, label2);
        mv.visitLabel(label3);
        mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.owner, Constants.MainWrapper, this.newMainDescriptor, false);
        Label label6 = new Label();
        mv.visitLabel(label6);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(6, 3);
        mv.visitEnd();
    }

    private boolean shouldBeInstrumented(String descriptorString) {
        String instrumented = this.instrumentationHelper.instrumentForNormalCall(descriptorString);
        return !instrumented.equals(descriptorString);
    }

    private boolean implementsInvocationHandler() {
        return Arrays.asList(this.interfaces).contains("java/lang/reflect/InvocationHandler");
    }

    private boolean overridesJdkSuperMethod(Method m) {
        return overriddenJdkSuperMethod(m) != null;
    }

    private Method overriddenJdkSuperMethod(Method m) {
        // TODO static methods
        boolean instrAccPublic = (m.getAccess() & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
        boolean instrAccProtected = (m.getAccess() & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED;
        boolean instrAccStatic = (m.getAccess() & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
        if (!(instrAccPublic || instrAccProtected) || instrAccStatic) {
            return null;
        }

        if (!this.isAnnotation) {
            Optional<Method> methodOptional = this.jdkMethods
                    .stream()
                    .filter(method -> method.equalsNameAndDescriptor(m))
                    .findAny();
            return methodOptional.orElse(null);
        }
        return null;
    }
}
