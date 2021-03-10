package com.sap.fontus.instrumentation;

import com.sap.fontus.instrumentation.strategies.InstrumentationStrategy;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.List;

public class SignatureTaintingVisitor extends SignatureVisitor {
    private final List<? extends InstrumentationStrategy> strategies;
    private final SignatureVisitor signatureVisitor;

    public SignatureTaintingVisitor(int api, List<? extends InstrumentationStrategy> strategies, SignatureVisitor signatureVisitor) {
        super(api);
        this.strategies = strategies;
        this.signatureVisitor = signatureVisitor;
    }

    public String instrumentName(final String name) {
        String instrumented = name;
        for (InstrumentationStrategy cis : this.strategies) {
            instrumented = cis.instrumentQN(instrumented);
        }
        return instrumented;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        this.signatureVisitor.visitFormalTypeParameter(instrumentName(name));
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new SignatureTaintingVisitor(this.api, this.strategies, this.signatureVisitor.visitClassBound());
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return this.signatureVisitor.visitInterfaceBound();
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new SignatureTaintingVisitor(this.api, this.strategies, this.signatureVisitor.visitSuperclass());
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new SignatureTaintingVisitor(this.api, this.strategies, this.signatureVisitor.visitInterface());
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return new SignatureTaintingVisitor(this.api, this.strategies, this.signatureVisitor.visitParameterType());
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new SignatureTaintingVisitor(this.api, this.strategies, this.signatureVisitor.visitReturnType());
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new SignatureTaintingVisitor(this.api, this.strategies, this.signatureVisitor.visitExceptionType());
    }

    @Override
    public void visitBaseType(char descriptor) {
        this.signatureVisitor.visitBaseType(descriptor);
    }

    @Override
    public void visitTypeVariable(String name) {
        this.signatureVisitor.visitTypeVariable(instrumentName(name));
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return new SignatureTaintingVisitor(this.api, this.strategies, this.signatureVisitor.visitArrayType());
    }

    @Override
    public void visitClassType(String name) {
        this.signatureVisitor.visitClassType(instrumentName(name));
    }

    @Override
    public void visitInnerClassType(String name) {
        this.signatureVisitor.visitInnerClassType(instrumentName(name));
    }

    @Override
    public void visitTypeArgument() {
        this.signatureVisitor.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        return new SignatureTaintingVisitor(this.api, this.strategies, this.signatureVisitor.visitTypeArgument(wildcard));
    }

    @Override
    public void visitEnd() {
        this.signatureVisitor.visitEnd();
    }
}
