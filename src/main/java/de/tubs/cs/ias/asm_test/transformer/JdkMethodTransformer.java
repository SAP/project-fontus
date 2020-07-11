package de.tubs.cs.ias.asm_test.transformer;

import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;
import de.tubs.cs.ias.asm_test.instrumentation.MethodTaintingVisitor;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.method.MethodInstrumentationStrategy;

import java.util.Collection;

public class JdkMethodTransformer implements ParameterTransformation, ReturnTransformation {

    private final FunctionCall call;
    private final Collection<MethodInstrumentationStrategy> instrumentation;
    private final Configuration configuration;

    public JdkMethodTransformer(FunctionCall call, Collection<MethodInstrumentationStrategy> instrumentation, Configuration configuration) {
        this.call = call;
        this.instrumentation = instrumentation;
        this.configuration = configuration;
    }

    @Override
    public void transform(int index, String type, MethodTaintingVisitor visitor) {

        for (MethodInstrumentationStrategy s : this.instrumentation) {
            s.insertJdkMethodParameterConversion(type);
        }

        FunctionCall converter = this.configuration.getConverterForParameter(this.call, index);
        if (converter != null) {
            visitor.visitMethodInsn(converter);
        }
    }

    @Override
    public void transform(MethodTaintingVisitor visitor, Descriptor desc) {

        for (MethodInstrumentationStrategy s : this.instrumentation) {
            s.instrumentReturnType(this.call.getOwner(), this.call.getName(), desc);
        }

        FunctionCall converter = this.configuration.getConverterForReturnValue(this.call);
        if (converter != null) {
            visitor.visitMethodInsn(converter);
        }

    }

}
