package de.tubs.cs.ias.asm_test.instrumentation;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.*;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintMethod;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.*;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz.*;
import de.tubs.cs.ias.asm_test.utils.*;
import de.tubs.cs.ias.asm_test.utils.lookups.AnnotationLookup;
import de.tubs.cs.ias.asm_test.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;


class ClassTaintingVisitor extends ClassVisitor {
    private static final Logger logger = LogUtils.getLogger();

    private final TaintStringConfig stringConfig;

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
    private final List<InstrumentationStrategy> instrumentation = new ArrayList<>(7);
    private final List<ClassInstrumentationStrategy> classInstrumentation = new ArrayList<>(7);
    private final Configuration config;
    private final ClassResolver resolver;
    /**
     * The name of the class currently processed.
     */
    private String owner;
    private String superName;
    private List<Method> jdkMethods;
    private final List<Method> overriddenJdkMethods;
    private String[] interfaces;
    private boolean isInterface;
    private final CombinedExcludedLookup combinedExcludedLookup;
    private final SignatureInstrumenter signatureInstrumenter;
    private final List<org.objectweb.asm.commons.Method> instrumentedMethods = new ArrayList<>();
    private boolean inEnd;
    private boolean extendsJdkSuperClass;

    public ClassTaintingVisitor(ClassVisitor cv, ClassResolver resolver, Configuration config, ClassLoader loader, boolean containsJSRRET) {
        super(Opcodes.ASM7, cv);
        this.visitor = cv;
        this.staticFinalFields = new ArrayList<>();
        this.overriddenJdkMethods = new ArrayList<>();
        this.resolver = resolver;
        this.loader = loader;
        this.config = config;
        this.containsJSRRET = containsJSRRET;
        this.stringConfig = this.config.getTaintStringConfig();
        this.newMainDescriptor = "(" + this.stringConfig.getTStringArrayDesc() + ")V";
        this.fillBlacklist();
        this.fillStrategies();
        this.signatureInstrumenter = new SignatureInstrumenter(this.api, this.instrumentation);
        this.combinedExcludedLookup = new CombinedExcludedLookup(loader);
    }

    private void fillStrategies() {
        this.classInstrumentation.add(new StringClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.classInstrumentation.add(new StringBufferClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.classInstrumentation.add(new StringBuilderClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.classInstrumentation.add(new PatternClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.classInstrumentation.add(new FormatterClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.classInstrumentation.add(new MatcherClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.classInstrumentation.add(new PropertiesClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.classInstrumentation.add(new ProxyClassInstrumentationStrategy(this.visitor));
        this.classInstrumentation.add(new DefaultClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));

        this.instrumentation.add(new StringInstrumentation(this.config.getTaintStringConfig()));
        this.instrumentation.add(new StringBuilderInstrumentation(this.config.getTaintStringConfig()));
        this.instrumentation.add(new StringBufferInstrumentation(this.config.getTaintStringConfig()));
        this.instrumentation.add(new PatternInstrumentation(this.config.getTaintStringConfig()));
        this.instrumentation.add(new FormatterInstrumentation(this.config.getTaintStringConfig()));
        this.instrumentation.add(new MatcherInstrumentation(this.config.getTaintStringConfig()));
        this.instrumentation.add(new PropertiesStrategy(this.config.getTaintStringConfig()));
        this.instrumentation.add(new ProxyInstrumentation());
        this.instrumentation.add(new DefaultInstrumentation(this.config.getTaintStringConfig()));
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
        this.superName = superName == null ? Type.getInternalName(Object.class) : superName;
        for (ClassInstrumentationStrategy cis : this.classInstrumentation) {
            this.superName = cis.instrumentSuperClass(this.superName);
        }
        this.interfaces = interfaces;

        this.isInterface = ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);

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

        if (version < Opcodes.V1_8 && !this.isAnnotation) {
            version = Opcodes.V1_8;
        }

        super.visit(version, access, name, instrumentedSignature, this.superName, interfaces);
    }

    private void initJdkClasses() {
        ClassTraverser classTraverser = new ClassTraverser(this.combinedExcludedLookup);
        if (!this.isAnnotation) {
            if (this.extendsJdkSuperClass) {
                classTraverser.getAllJdkMethods(this.superName, this.resolver);
            }
            classTraverser.addNotContainedJdkInterfaceMethods(this.superName, this.interfaces, this.resolver, this.loader);
        }
        this.jdkMethods = Collections.unmodifiableList(classTraverser.getMethods());
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

        FieldVisitor fv = null;
        for (ClassInstrumentationStrategy is : this.classInstrumentation) {
            Optional<FieldVisitor> ofv = is.instrumentFieldInstruction(
                    access, name, descriptor, signature, value,
                    (n, d, v) -> this.staticFinalFields.add(FieldData.of(n, d, v))
            );
            if (ofv.isPresent()) {
                fv = ofv.get();
                break;
            }
        }
        return fv;
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
        } else if (!this.isAnnotation && overridesJdkSuperMethod(access, name, descriptor) && shouldBeInstrumented(descriptor)) {
            logger.info("Creating proxy method for JDK inheritance for method: {}{}", name, descriptor);
            int newAccess = access & ~Opcodes.ACC_ABSTRACT;
            MethodVisitor v = super.visitMethod(newAccess, name, descriptor, signature, exceptions);

            this.overriddenJdkMethods.add(overriddenJdkSuperMethod(access, name, descriptor));

            this.generateProxyToInstrumented(v, newName, descriptor);

            desc = this.instrumentDescriptor(Descriptor.parseDescriptor(descriptor)).toDescriptor();
            mv = super.visitMethod(access, newName, desc, instrumentedSignature, exceptions);
        } else if (this.blacklist.contains(new BlackListEntry(name, descriptor, access))) {
            mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        } else if (this.instrumentedMethods.contains(new org.objectweb.asm.commons.Method(name, this.instrumentDescriptor(descriptor)))) {
            if (this.instrumentDescriptor(descriptor).equals(descriptor)) {
                // If a not instrumented method has been instrumented and afterwards the same method with a instrumented descriptor occurs, it has to be ignored
                return null;
            }
            int newAccess = access & ~Opcodes.ACC_ABSTRACT;
            MethodVisitor v = super.visitMethod(newAccess, name, descriptor, signature, exceptions);

            this.overriddenJdkMethods.add(overriddenJdkSuperMethod(access, name, descriptor));

            this.generateProxyToInstrumented(v, newName, descriptor);
            return null;
        } else {
            desc = this.instrumentDescriptor(Descriptor.parseDescriptor(descriptor)).toDescriptor();
            if (!desc.equals(descriptor)) {
                logger.info("Rewriting method signature {}{} to {}{}", name, descriptor, name, desc);
            }
            this.instrumentedMethods.add(new org.objectweb.asm.commons.Method(name, desc));
            mv = super.visitMethod(access, name, desc, instrumentedSignature, exceptions);
        }

        MethodTaintingVisitor mtv = new MethodTaintingVisitor(access, this.owner, newName, desc, mv, this.resolver, this.config, this.implementsInvocationHandler, this.instrumentation, this.combinedExcludedLookup);
        if (this.containsJSRRET) {
            return new JSRInlinerAdapter(mtv, access, newName, desc, instrumentedSignature, exceptions);
        }
        return mtv;
    }

    private void generateProxyToInstrumented(MethodVisitor mv, String instrumentedName, String originalDescriptorString) {
        Descriptor originalDescriptor = Descriptor.parseDescriptor(originalDescriptorString);
        Descriptor instrumentedDescriptor = this.instrumentDescriptor(originalDescriptor);
        mv.visitCode();
        // TODO Handle lists
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        int i = 1;
        for (String param : originalDescriptor.getParameters()) {

            mv.visitVarInsn(loadCodeByType(param), i);
            if (isDescriptorNameToInstrument(param)) {
                Type instrumentedType = Type.getType(this.instrumentQN(param));

                if (Type.getType(param).equals(Type.getType(String.class))) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.stringConfig.getTStringQN(), Constants.FROM_STRING, this.stringConfig.getFromStringDesc(), false);
                } else {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, instrumentedType.getInternalName());
                }
            }
            i += Type.getType(param).getSize();
        }

        int opcode;
        if ("<init>".equals(instrumentedName)) {
            opcode = Opcodes.INVOKESPECIAL;
        } else {
            opcode = Opcodes.INVOKEVIRTUAL;
        }

        mv.visitMethodInsn(opcode, this.owner, instrumentedName, instrumentedDescriptor.toDescriptor(), false);
        if (isDescriptorNameToInstrument(originalDescriptor.getReturnType())) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToOrigName, Constants.ConversionUtilsToOrigDesc, false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(originalDescriptor.getReturnType()).getInternalName());
        }

        mv.visitInsn(returnCodeByReturnType(originalDescriptor.getReturnType()));
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

    private int returnCodeByReturnType(String returnType) {
        return Type.getType(returnType).getOpcode(Opcodes.IRETURN);
    }

    private String getToOriginalMethod(String qn) {
        for (InstrumentationStrategy instrumentationStrategy : this.instrumentation) {
            if (instrumentationStrategy.handlesType(qn)) {
                return instrumentationStrategy.getGetOriginalTypeMethod();
            }
        }
        throw new IllegalArgumentException("Trying to get cast method for non instrumented type: " + qn);
    }

    private boolean isDescriptorNameToInstrument(String qn) {
        for (InstrumentationStrategy instrumentationStrategy : this.instrumentation) {
            if (instrumentationStrategy.handlesType(qn)) {
                return true;
            }
        }
        return false;
    }

    private String instrumentQN(String qn) {
        for (InstrumentationStrategy instrumentationStrategy : this.instrumentation) {
            if (instrumentationStrategy.handlesType(qn)) {
                return instrumentationStrategy.instrumentQN(qn);
            }
        }
        return qn;
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
        super.visitEnd();
    }

    private void declareMissingJdkMethods() {
        List<Method> methods = this.jdkMethods
                .stream()
                .filter(method -> !overriddenJdkMethods.contains(method))
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
        String descriptor = this.instrumentDescriptor(Descriptor.parseDescriptor(method.getDescriptor())).toDescriptor();
        String[] exceptions = method.getExceptions();
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (method.isDefault()) {
            this.generateInstrumentedProxyToSuper(mv, method, Descriptor.parseDescriptor(method.getDescriptor()), Descriptor.parseDescriptor(descriptor));
        }
    }

    private void overrideMissingJdkMethods() {
        List<Method> methods = this.jdkMethods
                .stream()
                .filter(method -> !overriddenJdkMethods.contains(method))
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
        Descriptor instrumentedDescriptor = this.instrumentDescriptor(originalDescriptor);

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
        MethodVisitor mv = super.visitMethod(m.getAccess(), m.getName(), instrumentedDescriptor.toDescriptor(), signature, exceptions);
        if (!Modifier.isAbstract(m.getAccess())) {
            this.generateInstrumentedProxyToSuper(mv, m, originalDescriptor, instrumentedDescriptor);
        }

        // Overriding method with original descriptor
        int modifiers = (m.getAccess() & ~Modifier.ABSTRACT);
        if (!Modifier.isFinal(m.getAccess())) {
            MethodVisitor mv2 = super.visitMethod(modifiers, m.getName(), originalDescriptor.toDescriptor(), signature, exceptions);
            this.generateProxyToInstrumented(mv2, m.getName(), originalDescriptor.toDescriptor());
        }
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
                Type instrumentedParam = Type.getType(this.instrumentQN(param));
                Type origParam = Type.getType(param);
                int arrayDimensions = calculateDescArrayDimensions(param);
                mv.visitInsn(Opcodes.DUP);
                Label label = new Label();
                mv.visitJumpInsn(Opcodes.IFNULL, label);
                if (arrayDimensions == 0) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, instrumentedParam.getInternalName());
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, instrumentedParam.getInternalName(), this.getToOriginalMethod(param), new Descriptor(new String[]{}, param).toDescriptor(), false);
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
            Type returnType = Type.getType(this.instrumentQN(origDescriptor.getReturnType()));

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
        mv.visitTypeInsn(Opcodes.ANEWARRAY, this.stringConfig.getTStringQN());
        mv.visitVarInsn(Opcodes.ASTORE, 1);
        Label label1 = new Label();
        mv.visitLabel(label1);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, 2);
        Label label2 = new Label();
        mv.visitLabel(label2);
        mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{this.stringConfig.getTStringArrayDesc(), Opcodes.INTEGER}, 0, null);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        Label label3 = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPGE, label3);
        Label label4 = new Label();
        mv.visitLabel(label4);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitTypeInsn(Opcodes.NEW, this.stringConfig.getTStringQN());
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.stringConfig.getTStringQN(), Constants.Init, Constants.TStringInitUntaintedDesc, false);
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

    /**
     * This instruments the descriptor to call the taintaware string classes (uses the interface types (e.g. IASStringable) for replacement)
     */
    private String instrumentDescriptorStringlike(String descriptor) {
        for (InstrumentationStrategy is : this.instrumentation) {
            descriptor = is.instrumentDesc(descriptor);
        }
        return descriptor;
    }

    /**
     * This instruments the descriptors for normal application classes (uses the actual taintaware classes (e.g. IASString))
     */
    private Descriptor instrumentDescriptor(Descriptor descriptor) {
        for (InstrumentationStrategy is : this.instrumentation) {
            descriptor = is.instrument(descriptor);
        }
        return descriptor;
    }

    private String instrumentDescriptor(String descriptor) {
        return this.instrumentDescriptor(Descriptor.parseDescriptor(descriptor)).toDescriptor();
    }

    private boolean shouldBeInstrumented(String descriptorString) {
        String instrumented = this.instrumentDescriptorStringlike(descriptorString);
        return !instrumented.equals(descriptorString);
    }

    private boolean implementsInvocationHandler() {
        return Arrays.asList(this.interfaces).contains("java/lang/reflect/InvocationHandler");
    }

    private boolean overridesJdkSuperMethod(int access, String name, String descriptor) {
        return overriddenJdkSuperMethod(access, name, descriptor) != null;
    }

    private Method overriddenJdkSuperMethod(int access, String name, String descriptor) {
        // TODO static methods
        boolean instrAccPublic = (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
        boolean instrAccProtected = (access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED;
        boolean instrAccStatic = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
        if (!(instrAccPublic || instrAccProtected) || instrAccStatic) {
            return null;
        }
        if (!this.isAnnotation) {
            Optional<Method> methodOptional = this.jdkMethods
                    .stream()
                    .filter(method -> method.getName().equals(name) && method.getDescriptor().equals(descriptor))
                    .findAny();
            return methodOptional.orElse(null);
        }
        return null;
    }
}
