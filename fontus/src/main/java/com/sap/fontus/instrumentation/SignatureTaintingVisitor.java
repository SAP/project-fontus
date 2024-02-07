package com.sap.fontus.instrumentation;

import org.objectweb.asm.signature.SignatureVisitor;

public class SignatureTaintingVisitor extends SignatureVisitor {
    private final SignatureVisitor signatureVisitor;
    private final InstrumentationHelper instrumentationHelper;

    public SignatureTaintingVisitor(int api, InstrumentationHelper instrumentationHelper, SignatureVisitor signatureVisitor) {
        super(api);
        this.instrumentationHelper = instrumentationHelper;
        this.signatureVisitor = signatureVisitor;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        this.signatureVisitor.visitFormalTypeParameter(this.instrumentationHelper.instrumentQN(name));
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new SignatureTaintingVisitor(this.api, this.instrumentationHelper, this.signatureVisitor.visitClassBound());
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return this.signatureVisitor.visitInterfaceBound();
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new SignatureTaintingVisitor(this.api, this.instrumentationHelper, this.signatureVisitor.visitSuperclass());
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new SignatureTaintingVisitor(this.api, this.instrumentationHelper, this.signatureVisitor.visitInterface());
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return new SignatureTaintingVisitor(this.api, this.instrumentationHelper, this.signatureVisitor.visitParameterType());
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new SignatureTaintingVisitor(this.api, this.instrumentationHelper, this.signatureVisitor.visitReturnType());
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new SignatureTaintingVisitor(this.api, this.instrumentationHelper, this.signatureVisitor.visitExceptionType());
    }

    @Override
    public void visitBaseType(char descriptor) {
        this.signatureVisitor.visitBaseType(descriptor);
    }

    @Override
    public void visitTypeVariable(String name) {
        this.signatureVisitor.visitTypeVariable(this.instrumentationHelper.instrumentQN(name));
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return new SignatureTaintingVisitor(this.api, this.instrumentationHelper, this.signatureVisitor.visitArrayType());
    }

    @Override
    public void visitClassType(String name) {
        this.signatureVisitor.visitClassType(this.instrumentationHelper.instrumentQN(name));
    }

    @Override
    public void visitInnerClassType(String name) {
        this.signatureVisitor.visitInnerClassType(this.instrumentationHelper.instrumentQN(name));
    }

    @Override
    public void visitTypeArgument() {
        this.signatureVisitor.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        return new SignatureTaintingVisitor(this.api, this.instrumentationHelper, this.signatureVisitor.visitTypeArgument(wildcard));
    }

    @Override
    public void visitEnd() {
        this.signatureVisitor.visitEnd();
    }
}
