package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.strategies.InstrumentationHelper;
import com.sap.fontus.utils.ClassTraverser;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LambdaCall implements Serializable {
    /**
     * Internal name of the functional interface
     */
    private final Type functionalInterface;
    private final FunctionCall implementation;

    public LambdaCall(Type functionalInterface, FunctionCall implementation) {
        this.functionalInterface = functionalInterface;
        this.implementation = implementation;
    }

    public Type getFunctionalInterface() {
        return functionalInterface;
    }

    public FunctionCall getImplementation() {
        return implementation;
    }

    public Descriptor getProxyDescriptor(ClassLoader loader, InstrumentationHelper instrumentationHelper) {
        ClassResolver classResolver = new ClassResolver(loader);
        ClassTraverser clsTrvs = new ClassTraverser(new CombinedExcludedLookup(null));
        clsTrvs.readMethods(this.functionalInterface, classResolver);
        List<Method> allMethods = clsTrvs.getMethods();

        ClassTraverser objectTrvs = new ClassTraverser(new CombinedExcludedLookup(null));
        objectTrvs.readMethods(Type.getType(Object.class), classResolver);
        List<Method> objMethods = objectTrvs.getMethods();

        List<Method> methods = allMethods
                .stream()
                .filter((m) -> (m.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
                .filter((m) -> objMethods.stream().noneMatch(om -> m.getName().equals(om.getName()) && m.getDescriptor().equals(om.getDescriptor())))
                .collect(Collectors.toList());

        if (methods.size() != 1) {
            throw new IllegalArgumentException("Functional interface of lambda call does not have exactly one method: " + this.functionalInterface.getClassName());
        }

        Method method = methods.get(0);

        Descriptor interfaceDescriptor = method.getParsedDescriptor();

        Descriptor implementationDesc = this.implementation.getParsedDescriptor();
        Descriptor instrumentedImplementationDesc = instrumentationHelper.instrumentForNormalCall(implementationDesc);

        List<String> mergedParameters = new ArrayList<>(implementationDesc.parameterCount());

        for (int i = 0; i < instrumentedImplementationDesc.parameterCount(); i++) {
            int enclosedCount = instrumentedImplementationDesc.parameterCount() - interfaceDescriptor.parameterCount();
            if (i < enclosedCount) {
                mergedParameters.add(instrumentedImplementationDesc.getParameters().get(i));
            } else {
                Type implementationParam = Type.getType(implementationDesc.getParameters().get(i));

                if (instrumentationHelper.isInstrumented(implementationParam.getDescriptor())) {
                    String uninstrumented = instrumentationHelper.uninstrumentNormalCall(implementationParam.getDescriptor());
                    mergedParameters.add(uninstrumented);
                } else {
                    mergedParameters.add(implementationParam.getDescriptor());
                }

            }
        }

        Type implementationReturn = Type.getType(instrumentedImplementationDesc.getReturnType());

        Type returnType;
        if (instrumentationHelper.isInstrumented(implementationReturn.getDescriptor())) {
            String uninstrumented = instrumentationHelper.uninstrumentNormalCall(implementationReturn.getDescriptor());
            returnType = Type.getType(uninstrumented);
        } else {
            returnType = implementationReturn;
        }

        return new Descriptor(mergedParameters.toArray(new String[]{}), returnType.getDescriptor());
    }
}
