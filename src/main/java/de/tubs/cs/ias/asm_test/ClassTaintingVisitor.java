package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.asm.ClassInitializerAugmentingVisitor;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.asm.MethodVisitRecording;
import de.tubs.cs.ias.asm_test.asm.RecordingMethodVisitor;
import de.tubs.cs.ias.asm_test.strategies.clazz.*;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


class ClassTaintingVisitor extends ClassVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TaintStringConfig stringConfig;

    private final Collection<BlackListEntry> blacklist = new ArrayList<>();
    private final String newMainDescriptor;
    private final Collection<FieldData> staticFinalFields;
    private boolean hasClInit = false;
    private boolean lacksToString = true;
    private boolean isAnnotation = false;
    private MethodVisitRecording recording;
    private final ClassVisitor visitor;
    private final List<ClassInstrumentationStrategy> instrumentation = new ArrayList<>(4);
    private final Configuration config;
    private final ClassResolver resolver;
    /**
     * The name of the class currently processed.
     */
    private String owner;
    private String superName;

    public ClassTaintingVisitor(ClassVisitor cv, ClassResolver resolver, Configuration config) {
        super(Opcodes.ASM7, cv);
        this.visitor = cv;
        this.staticFinalFields = new ArrayList<>();
        this.resolver = resolver;
        this.config = config;
        this.stringConfig = this.config.getTaintStringConfig();
        this.newMainDescriptor = "(" + this.stringConfig.getTStringArrayDesc() + ")V";
        this.fillBlacklist();
        this.fillStrategies();
    }

    private void fillStrategies() {
        this.instrumentation.add(new FormatterClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.instrumentation.add(new MatcherClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.instrumentation.add(new PatternClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.instrumentation.add(new StringBufferClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.instrumentation.add(new StringBuilderClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.instrumentation.add(new StringClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
        this.instrumentation.add(new DefaultClassInstrumentationStrategy(this.visitor, this.config.getTaintStringConfig()));
    }

    private void fillBlacklist() {
        //this.blacklist.add(new BlackListEntry("main", Constants.MAIN_METHOD_DESC, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC));
        this.blacklist.add(new BlackListEntry(Constants.ToString, Constants.ToStringDesc, Opcodes.ACC_PUBLIC));
    }

    /**
     * To add the wrapped main method we need to record the class name we currently are operating on.
     */
    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        this.owner = name;
        this.superName = superName == null ? Type.getInternalName(Object.class) : superName;
        if ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) {
            this.lacksToString = false;
        }

        // Is this class/interface an annotation or annotation proxy class? If yes, don't instrument it
        // Cf Java Language Specification 12 - 9.6.1 Annotation Types
        if (Utils.contains(interfaces, Constants.AnnotationQN) ||
                (Constants.ProxyQN.equals(superName)
                        && interfaces.length == 1
                        && InstrumentationState.getInstance().isAnnotation(interfaces[0], this.resolver)
                )
        ) {
            logger.info("{} is annotation or annotation proxy!", name);
            this.isAnnotation = true;
            this.lacksToString = false;
            InstrumentationState.getInstance().addAnnotation(name);
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Writes all static final String field initializations into the static initializer
     *
     * @param mv The visitor creating the static initialization block
     */
    private void writeToStaticInitializer(MethodVisitor mv) {
        for (FieldData e : this.staticFinalFields) {
            Object value = e.getValue();
            mv.visitLdcInsn(value);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, this.owner, e.getName(), e.getDescriptor());
        }
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
        for (ClassInstrumentationStrategy is : this.instrumentation) {
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
     * Generate the body of the toString proxy method
     * <p>
     * To not break OOP we can't change the Signature of the toString method inherited from Object.
     * Our solution is thus to instrument the toString method and rename it to $toString with descriptor ()TString;
     * <p>
     * To provide a working toString method (i.e., not break applications by having objects use the default toString method)
     * the code generated here calls $toString, check whether it was tainted and returns the regular String.
     * <p>
     * This loses the taint! Thus passing instrumented objects to JVM standard library functions which call toString need special handling.
     * One solution is to write proxy methods that reassemble the taint information afterwards.
     * <p>
     * TODO: The taint handling probably needs various levels of action when a tainted String is required.
     * This function could just log that the taint is lost and thus notifies the developer that a proxy might be required.
     * Maybe a whitelist where losing the taint is fine would be a good idea? Where the toString is called,
     * can be detected by inspecting the call stack. This might be really slow however..
     *
     * @param mv The visitor, visiting the toString method.
     */
    private void generateToStringProxy(MethodVisitor mv) {
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.owner, Constants.ToStringInstrumented, this.stringConfig.getToStringInstrumentedDesc(), false);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.stringConfig.getTStringQN(), Constants.TStringToStringName, Constants.ToStringDesc, false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(6, 3);
        mv.visitEnd();
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
        MethodVisitor mv;
        String desc = descriptor;
        String newName = name;

        if (this.recording == null && isClInit(access, name, desc)) {
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
            MethodVisitor v = super.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", Constants.MAIN_METHOD_DESC, signature, exceptions);
            this.createMainWrapperMethod(v);
            logger.info("Processing renamed main method.");
            mv = super.visitMethod(access, Constants.MainWrapper, this.newMainDescriptor, signature, exceptions);
            newName = Constants.MainWrapper;
            desc = this.newMainDescriptor;
        } else if (access == Opcodes.ACC_PUBLIC && Constants.ToString.equals(name) && Constants.ToStringDesc.equals(descriptor)) {
            this.lacksToString = false;
            logger.info("Creating proxy toString method");
            MethodVisitor v = super.visitMethod(Opcodes.ACC_PUBLIC, Constants.ToString, Constants.ToStringDesc, signature, exceptions);
            this.generateToStringProxy(v);
            newName = Constants.ToStringInstrumented;
            desc = this.stringConfig.getToStringInstrumentedDesc();
            mv = super.visitMethod(access, newName, desc, signature, exceptions);
        } else if (overridesJdkSuperMethod(access, name, descriptor) && shouldBeInstrumented(descriptor)) {
            logger.info("Creating proxy method for JDK inheritance");
            MethodVisitor v = super.visitMethod(access, name, descriptor, signature, exceptions);
            newName = this.rewriteMethodNameForJdkInheritanceProxy(name);

            this.generateJdkInheritanceProxy(v, newName, descriptor);

            desc = this.instrumentDescriptor(descriptor);
            mv = super.visitMethod(access, newName, desc, signature, exceptions);
        } else if (this.blacklist.contains(new BlackListEntry(name, descriptor, access))) {
            mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        } else {
            Descriptor d = Descriptor.parseDescriptor(descriptor);
            for (ClassInstrumentationStrategy is : this.instrumentation) {
                d = is.instrument(d);
            }
            desc = d.toDescriptor();
            if (!desc.equals(descriptor)) {
                logger.info("Rewriting method signature {}{} to {}{}", name, descriptor, name, desc);
            }
            mv = super.visitMethod(access, name, desc, signature, exceptions);
        }

        return new MethodTaintingVisitor(access, newName, desc, mv, this.resolver, this.config);
    }

    private void generateJdkInheritanceProxy(MethodVisitor mv, String instrumentedName, String descriptor) {
        Descriptor d = Descriptor.parseDescriptor(descriptor);
        String instrumentedDescriptor = this.instrumentDescriptor(descriptor);
        mv.visitCode();
        // TODO Labels
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        int i = 1;
        for (String param : d.getParameters()) {

            // Creating new Object if necessary and duplicating it for initialization
            if (isQNToInstrument(param)) {
                String instrumentedParam = Descriptor.descriptorNameToQN(this.instrumentQN(param));
                mv.visitTypeInsn(Opcodes.NEW, instrumentedParam);
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(loadCodeByType(param), i);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, instrumentedParam, Constants.Init, new Descriptor(new String[]{param}, "V").toDescriptor(), false);
            } else {
                mv.visitVarInsn(loadCodeByType(param), i);
            }
            i++;
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.owner, instrumentedName, instrumentedDescriptor, false);
        if (isQNToInstrument(d.getReturnType())) {
            String returnType = Descriptor.descriptorNameToQN(this.instrumentQN(d.getReturnType()));
            String toOriginalMethod = this.getToOriginalMethod(returnType);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, returnType, toOriginalMethod, new Descriptor(d.getReturnType()).toDescriptor(), false);
        }

        mv.visitInsn(returnCodeByReturnType(d.getReturnType()));
        mv.visitMaxs(d.parameterCount() + 1, d.parameterCount() + 1);
        mv.visitEnd();
    }

    private int loadCodeByType(String type) {
        switch (type) {
            case "I":
            case "B":
            case "S":
            case "Z":
                return Opcodes.ILOAD;
            case "J":
                return Opcodes.LLOAD;
            case "F":
                return Opcodes.FLOAD;
            case "D":
                return Opcodes.DLOAD;
            default:
                return Opcodes.ALOAD;
        }
    }

    private int returnCodeByReturnType(String returnType) {
        switch (returnType) {
            case "V":
                return Opcodes.RETURN;
            case "I":
            case "B":
            case "S":
            case "Z":
                return Opcodes.IRETURN;
            case "J":
                return Opcodes.LRETURN;
            case "F":
                return Opcodes.FRETURN;
            case "D":
                return Opcodes.DRETURN;
            default:
                return Opcodes.ARETURN;
        }
    }

    private String getToOriginalMethod(String qn) {
        for (ClassInstrumentationStrategy instrumentationStrategy : this.instrumentation) {
            if (instrumentationStrategy.handlesType(qn)) {
                return instrumentationStrategy.getGetOriginalTypeMethod();
            }
        }
        throw new IllegalArgumentException("Trying to get cast method for non instrumented type: " + qn);
    }

    private boolean isQNToInstrument(String qn) {
        for (ClassInstrumentationStrategy instrumentationStrategy : this.instrumentation) {
            if (instrumentationStrategy.handlesType(qn)) {
                return true;
            }
        }
        return false;
    }

    private String instrumentQN(String qn) {
        for (ClassInstrumentationStrategy instrumentationStrategy : this.instrumentation) {
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
        if (this.lacksToString) {
            logger.info("Adding missing toString method");
            MethodVisitor v = super.visitMethod(Opcodes.ACC_PUBLIC, Constants.ToStringInstrumented, this.stringConfig.getToStringInstrumentedDesc(), null, null);
            this.createToString(v);
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

    private void createToString(MethodVisitor mv) {
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        Objects.requireNonNull(this.superName);
        if (JdkClassesLookupTable.getInstance().isJdkClass(this.superName)) {
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.superName, Constants.ToString, Constants.ToStringDesc, false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.stringConfig.getTStringQN(), Constants.FROM_STRING, this.stringConfig.getFromStringDesc(), false);
        } else {
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.superName, Constants.ToStringInstrumented, this.stringConfig.getToStringInstrumentedDesc(), false);
        }
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(6, 3);
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

    private Descriptor instrumentDescriptor(Descriptor d) {
        for (ClassInstrumentationStrategy is : this.instrumentation) {
            d = is.instrument(d);
        }
        return d;
    }

    private String instrumentDescriptor(String descriptor) {
        Descriptor d = Descriptor.parseDescriptor(descriptor);
        d = this.instrumentDescriptor(d);
        return d.toDescriptor();
    }

    private boolean shouldBeInstrumented(String descriptorString) {
        String instrumented = this.instrumentDescriptor(descriptorString);
        return !instrumented.equals(descriptorString);
    }

    private boolean inheritsFromJdkClass() {
        return !isAnnotation && JdkClassesLookupTable.getInstance().isJdkClass(superName);
    }

    // TODO What if not the class does not inherit directly from JDK?
    private boolean overridesJdkSuperMethod(int access, String name, String descriptor) {
        boolean instrAccPublic = (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
        boolean instrAccProtected = (access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED;
        if (!(instrAccPublic || instrAccProtected)) {
            return false;
        }
        if (inheritsFromJdkClass()) {
            Class<?> superClass;
            try {
                superClass = Class.forName(superName.replace('/', '.'));
            } catch (ClassNotFoundException e) {
                return false;
            }
            Method[] methods = superClass.getDeclaredMethods();
            for (Method m : methods) {
                boolean nameEquals = m.getName().equals(name);
                boolean isPublicOrProtected = Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers());
                boolean descriptorEquals = Descriptor.parseDescriptor(descriptor).equals(getMethodDescriptor(m));
                if (nameEquals && isPublicOrProtected && descriptorEquals) {
                    return true;
                }
            }
        }
        return false;
    }

    private String rewriteMethodNameForJdkInheritanceProxy(String name) {
        // TODO is another name necessary?
        return name;
    }

    private Descriptor getMethodDescriptor(Method m) {
        String[] params = new String[m.getParameterCount()];
        for (int i = 0; i < params.length; i++) {
            params[i] = Descriptor.classNameToDescriptorName(m.getParameters()[i].getAnnotatedType().getType().getTypeName());
        }
        String returnType = Descriptor.classNameToDescriptorName(m.getAnnotatedReturnType().getType().toString());
        return new Descriptor(params, returnType);
    }
}
