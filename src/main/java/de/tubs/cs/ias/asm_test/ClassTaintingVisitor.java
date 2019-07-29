package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.classinstumentation.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;


class ClassTaintingVisitor extends ClassVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Collection<BlackListEntry> blacklist = new ArrayList<>();
    private static final String mainDescriptor = "([Ljava/lang/String;)V";
    private static final String newMainDescriptor = "(" + Constants.TStringArrayDesc + ")V";
    private final Collection<Tuple<Tuple<String, String>, Object>> staticFinalFields;
    private boolean hasClInit = false;
    private MethodVisitRecording recording;
    private final ClassVisitor visitor;
    private final Collection<ClassInstrumentationStrategy> instrumentation = new ArrayList<>(4);

    /**
     * The name of the class currently processed.
     */
    private String owner;

    ClassTaintingVisitor(ClassVisitor cv) {
        super(Opcodes.ASM7, cv);
        this.visitor = cv;
        this.staticFinalFields = new ArrayList<>();
        this.fillBlacklist();
        this.fillStrategies();
    }

    private void fillStrategies() {
        this.instrumentation.add(new StringBufferClassInstrumentationStrategy(this.visitor));
        this.instrumentation.add(new StringBuilderClassInstrumentationStrategy(this.visitor));
        this.instrumentation.add(new StringClassInstrumentationStrategy(this.visitor));
        this.instrumentation.add(new DefaultClassInstrumentationStrategy(this.visitor));
    }

    private void fillBlacklist() {
        this.blacklist.add(new BlackListEntry("main", mainDescriptor, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC));
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
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Writes all static final String field initializations into the static initializer
     *
     * @param mv The visitor creating the static initialization block
     */
    private void writeToStaticInitializer(MethodVisitor mv) {
        for (Tuple<Tuple<String, String>, Object> e : this.staticFinalFields) {
            Object value = e.y;
            Tuple<String, String> field = e.x;
            mv.visitLdcInsn(value);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, this.owner, field.x, field.y);
        }
    }

    /**
     * Replaces String like attributes with their taint-aware counterparts.
     */
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor,
                                   String signature, Object value) {

        FieldVisitor fv = null;
        for (ClassInstrumentationStrategy is : this.instrumentation) {
            Optional<FieldVisitor> ofv = is.instrumentFieldInstruction(
                    access, name, descriptor, signature, value,
                    (n, d, v) -> this.staticFinalFields.add(Tuple.of(Tuple.of(n, d), v))
            );
            if (ofv.isPresent()) {
                fv = ofv.get();
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
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.owner, Constants.ToStringInstrumented, Constants.ToStringInstrumentedDesc, false);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.ABORT_IF_TAINTED, "()V", false);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.TStringToStringName, Constants.ToStringDesc, false);
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
        if (((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) && (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC && "main".equals(name) && descriptor.equals(mainDescriptor)) {
            logger.info("Creating proxy main method");
            MethodVisitor v = super.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", mainDescriptor, signature, exceptions);
            this.createMainWrapperMethod(v);
            logger.info("Processing renamed main method.");
            mv = super.visitMethod(access, Constants.MainWrapper, newMainDescriptor, signature, exceptions);
            newName = Constants.MainWrapper;
            desc = newMainDescriptor;
        } else if (access == Opcodes.ACC_PUBLIC && Constants.ToString.equals(name) && Constants.ToStringDesc.equals(descriptor)) {
            logger.info("Creating proxy toString method");
            MethodVisitor v = super.visitMethod(Opcodes.ACC_PUBLIC, Constants.ToString, Constants.ToStringDesc, signature, exceptions);
            this.generateToStringProxy(v);
            newName = Constants.ToStringInstrumented;
            desc = Constants.ToStringInstrumentedDesc;
            mv = super.visitMethod(access, newName, desc, signature, exceptions);
        } else if (this.blacklist.contains(new BlackListEntry(name, descriptor, access))) {
            mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        } else {
            Descriptor d = Descriptor.parseDescriptor(descriptor);
            for (ClassInstrumentationStrategy is : this.instrumentation) {
                d = is.instrumentMethodInvocation(d);
            }
            desc = d.toDescriptor();
            if (!desc.equals(descriptor)) {
                logger.info("Rewriting method signature {}{} to {}{}", name, descriptor, name, desc);
            }
            mv = super.visitMethod(access, name, desc, signature, exceptions);
        }

        return new MethodTaintingVisitor(access, newName, desc, mv);
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
        mv.visitTypeInsn(Opcodes.ANEWARRAY, Constants.TStringQN);
        mv.visitVarInsn(Opcodes.ASTORE, 1);
        Label label1 = new Label();
        mv.visitLabel(label1);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, 2);
        Label label2 = new Label();
        mv.visitLabel(label2);
        mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Constants.TStringArrayDesc, Opcodes.INTEGER}, 0, null);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        Label label3 = new Label();
        mv.visitJumpInsn(Opcodes.IF_ICMPGE, label3);
        Label label4 = new Label();
        mv.visitLabel(label4);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitTypeInsn(Opcodes.NEW, Constants.TStringQN);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringQN, Constants.Init, Constants.TStringInitUntaintedDesc, false);
        mv.visitInsn(Opcodes.AASTORE);
        Label label5 = new Label();
        mv.visitLabel(label5);
        mv.visitIincInsn(2, 1);
        mv.visitJumpInsn(Opcodes.GOTO, label2);
        mv.visitLabel(label3);
        mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.owner, Constants.MainWrapper, newMainDescriptor, false);
        Label label6 = new Label();
        mv.visitLabel(label6);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(6, 3);
        mv.visitEnd();
    }

}
