package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.*;

/**
 * A MethodVisitor that stores all visitXXX calls in a MethodVisitRecording instance.
 * <p>
 * It is important that processing of Annotations for the visited Method is <b>NOT</b> supported!
 */
@SuppressWarnings("ReturnOfNull")
public class RecordingMethodVisitor extends MethodVisitor {

    private final MethodVisitRecording recording;

    RecordingMethodVisitor() {
        super(Opcodes.ASM7);
        this.recording = new MethodVisitRecording();
    }

    MethodVisitRecording getRecording() {
        return this.recording;
    }


    @Override
    public void visitParameter(String name, int access) {
        this.recording.add(v -> v.visitParameter(name, access));
    }


    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        this.recording.add(MethodVisitor::visitAnnotationDefault);
        return null;
    }


    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        this.recording.add(v -> v.visitAnnotation(descriptor, visible));
        return null;
    }


    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.recording.add(v -> v.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
        return null;
    }


    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        this.recording.add(v -> v.visitAnnotableParameterCount(parameterCount, visible));
    }


    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        this.recording.add(v -> v.visitParameterAnnotation(parameter, descriptor, visible));
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        this.recording.add(v -> v.visitAttribute(attribute));
    }


    @Override
    public void visitCode() {
        this.recording.add(MethodVisitor::visitCode);
    }


    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        this.recording.add(v -> v.visitFrame(type, numLocal, local, numStack, stack));
    }


    @Override
    public void visitInsn(int opcode) {
        this.recording.add(v -> v.visitInsn(opcode));
    }


    @Override
    public void visitIntInsn(int opcode, int operand) {
        this.recording.add(v -> v.visitIntInsn(opcode, operand));
    }


    @Override
    public void visitVarInsn(int opcode, int var) {
        this.recording.add(v -> v.visitVarInsn(opcode, var));
    }


    @Override
    public void visitTypeInsn(int opcode, String type) {
        this.recording.add(v -> v.visitTypeInsn(opcode, type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        this.recording.add(v -> v.visitFieldInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        this.recording.add(v -> v.visitMethodInsn(opcode, owner, name, descriptor));
    }


    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        this.recording.add(v -> v.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }


    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        this.recording.add(v -> v.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }


    @Override
    public void visitJumpInsn(int opcode, Label label) {
        this.recording.add(v -> v.visitJumpInsn(opcode, label));
    }


    @Override
    public void visitLabel(Label label) {
        this.recording.add(v -> v.visitLabel(label));
    }


    @Override
    public void visitLdcInsn(Object value) {
        this.recording.add(v -> v.visitLdcInsn(value));
    }


    @Override
    public void visitIincInsn(int var, int increment) {
        this.recording.add(v -> v.visitIincInsn(var, increment));
    }


    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        this.recording.add(v -> v.visitTableSwitchInsn(min, max, dflt, labels));
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        this.recording.add(v -> v.visitLookupSwitchInsn(dflt, keys, labels));
    }


    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        this.recording.add(v -> v.visitMultiANewArrayInsn(descriptor, numDimensions));
    }


    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.recording.add(v -> v.visitInsnAnnotation(typeRef, typePath, descriptor, visible));
        return null;
    }


    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.recording.add(v -> v.visitTryCatchBlock(start, end, handler, type));
    }


    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        this.recording.add(v -> v.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible));
        return null;
    }


    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        this.recording.add(v -> v.visitLocalVariable(name, descriptor, signature, start, end, index));
    }


    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        this.recording.add(v -> v.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible));
        return null;
    }


    @Override
    public void visitLineNumber(int line, Label start) {
        this.recording.add(v -> v.visitLineNumber(line, start));
    }


    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        this.recording.add(v -> v.visitMaxs(maxStack, maxLocals));
    }

    @Override
    public void visitEnd() {
        this.recording.add(MethodVisitor::visitEnd);
    }
}
