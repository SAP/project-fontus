package com.sap.fontus.instrumentation;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.asm.resolver.IClassResolver;
import com.sap.fontus.utils.ClassTraverser;
import com.sap.fontus.asm.resolver.ClassResolverFactory;
import com.sap.fontus.utils.lookups.AnnotationLookup;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LambdaCall implements Serializable {
    /**
     * Internal name of the functional interface
     */
    private final Type functionalInterface;
    private final Type invokeDescriptor;
    private Type concreteImplementationType;
    private final FunctionCall implementation;
    private final Handle implementationHandle;

    public LambdaCall(Type functionalInterface, Handle implementationHandle, Type invokeDescriptor) {
        this.functionalInterface = functionalInterface;
        this.implementation = FunctionCall.fromHandle(implementationHandle);
        this.implementationHandle = implementationHandle;
        this.invokeDescriptor = invokeDescriptor;
    }

    public void setConcreteImplementationType(Type concreteImplementationType) {
        this.concreteImplementationType = concreteImplementationType;
    }

    public Type getFunctionalInterface() {
        return this.functionalInterface;
    }

    public FunctionCall getImplementation() {
        return this.implementation;
    }

    public Handle getImplementationHandle() {
        return this.implementationHandle;
    }

    public Type getInvokeDescriptor() { return this.invokeDescriptor; }

    private Type getConcreteOrOwnerImplementation() {
        return this.concreteImplementationType == null ? Type.getObjectType(this.implementation.getOwner()) : this.concreteImplementationType;
    }

    public Descriptor getProxyDescriptor(ClassLoader loader, InstrumentationHelper instrumentationHelper) {
        CombinedExcludedLookup lookup = new CombinedExcludedLookup(loader);
        IClassResolver classResolver = ClassResolverFactory.createClassResolver(loader);
        ClassTraverser clsTrvs = new ClassTraverser(new CombinedExcludedLookup(null));
        clsTrvs.readMethods(this.functionalInterface, classResolver);
        Set<Method> allMethods = clsTrvs.getMethods();

        ClassTraverser objectTrvs = new ClassTraverser(new CombinedExcludedLookup(null));
        objectTrvs.readMethods(Type.getType(Object.class), classResolver);
        Set<Method> objMethods = objectTrvs.getMethods();

        List<Method> methods = allMethods
                .stream()
                .filter((m) -> (m.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
                .filter((m) -> !Constants.ObjectQN.equals(m.getOwner()))
                .filter((m) -> objMethods.stream().noneMatch(om -> m.getName().equals(om.getName()) && m.getDescriptor().equals(om.getDescriptor())))
                .collect(Collectors.toList());

        int enclosedCount;
        if (methods.size() > 1) {
            throw new IllegalArgumentException("Functional interface of lambda call has more than one method: " + this.functionalInterface.getClassName());
        } else if (methods.size() == 1) {
            Method method = methods.get(0);
            enclosedCount = this.implementation.getParsedDescriptor().parameterCount() - method.getParsedDescriptor().parameterCount();
        } else {
            if (this.invokeDescriptor.getArgumentTypes().length >= 1) {
                if (this.invokeDescriptor.getArgumentTypes()[0].equals(Type.getObjectType(this.implementation.getOwner()))) {
                    enclosedCount = this.invokeDescriptor.getArgumentTypes().length - 1;
                } else {
                    enclosedCount = this.invokeDescriptor.getArgumentTypes().length;
                }
            } else {
                enclosedCount = 0;
            }
        }

        // Type.getDescriptor will give a class name back like Ljava/lang/Integer; so need to convert it
        // Also check whether the class is an annotation, which we also do not instrument, and therefore need to proxy
        String descriptor = Descriptor.removeLeadingLandTrailingSemiColon(getConcreteOrOwnerImplementation().getDescriptor());
        if (lookup.isPackageExcludedOrJdkOrAnnotation(descriptor)) {
            return this.generateProxyToJdkDescriptor(instrumentationHelper);
        } else {
            return this.generateProxyToInstrumentedDescriptor(enclosedCount, instrumentationHelper);
        }
    }

    private Descriptor generateProxyToJdkDescriptor(InstrumentationHelper instrumentationHelper) {
        Descriptor implementationDesc = this.implementation.getParsedDescriptor();
        Descriptor instrumentedImplementationDesc = instrumentationHelper.instrument(implementationDesc);

        List<String> proxyParameters = new ArrayList<>(implementationDesc.parameterCount());

        if (this.isInstanceCall()) {
            String concreteOwnerOrImplementation = getConcreteOrOwnerImplementation().getDescriptor();
            String instrumentedConcreteOwnerOrImplementation = instrumentationHelper.instrument(concreteOwnerOrImplementation);
            proxyParameters.add(instrumentedConcreteOwnerOrImplementation);
        }

        for (int i = 0; i < instrumentedImplementationDesc.parameterCount(); i++) {
            Type implementationParam = Type.getType(implementationDesc.getParameters().get(i));

            if (instrumentationHelper.canHandleType(implementationParam.getDescriptor())) {
                String instrumented = instrumentationHelper.instrumentQN(implementationParam.getDescriptor());
                proxyParameters.add(instrumented);
            } else {
                proxyParameters.add(implementationParam.getDescriptor());
            }
        }

        Type implementationReturn;
        if (this.isConstructorCall()) {
            implementationReturn = Type.getObjectType(this.implementationHandle.getOwner());
        } else {
            implementationReturn = Type.getType(instrumentedImplementationDesc.getReturnType());
        }

        Type returnType;
        if (instrumentationHelper.isInstrumented(implementationReturn.getDescriptor())) {
            String instrumented = instrumentationHelper.instrumentQN(implementationReturn.getDescriptor());
            returnType = Type.getType(instrumented);
        } else {
            returnType = implementationReturn;
        }

        return new Descriptor(proxyParameters.toArray(new String[]{}), returnType.getDescriptor());
    }

    private Descriptor generateProxyToInstrumentedDescriptor(int enclosedCount, InstrumentationHelper instrumentationHelper) {
        Descriptor implementationDesc = this.implementation.getParsedDescriptor();
        Descriptor instrumentedImplementationDesc = instrumentationHelper.instrument(implementationDesc);

        List<String> mergedParameters = new ArrayList<>(implementationDesc.parameterCount());

        if (this.isInstanceCall()) {
            String concreteOwnerOrImplementation = getConcreteOrOwnerImplementation().getDescriptor();
            String instrumentedConcreteOwnerOrImplementation = instrumentationHelper.instrument(concreteOwnerOrImplementation);
            mergedParameters.add(instrumentedConcreteOwnerOrImplementation);
        }
        for (int i = 0; i < enclosedCount; i++) {
            mergedParameters.add(instrumentedImplementationDesc.getParameters().get(i));
        }
        for (int i = Math.max(enclosedCount, 0); i < instrumentedImplementationDesc.parameterCount(); i++) {
            Type implementationParam = Type.getType(implementationDesc.getParameters().get(i));

            if (instrumentationHelper.isInstrumented(implementationParam.getDescriptor())) {
                String uninstrumented = instrumentationHelper.uninstrument(implementationParam.getDescriptor());
                mergedParameters.add(uninstrumented);
            } else {
                mergedParameters.add(implementationParam.getDescriptor());
            }
        }


        Type implementationReturn;
        if (this.isConstructorCall()) {
            implementationReturn = Type.getObjectType(this.implementationHandle.getOwner());
        } else {
            implementationReturn = Type.getType(instrumentedImplementationDesc.getReturnType());
        }

        Type returnType;
        if (instrumentationHelper.isInstrumented(implementationReturn.getDescriptor())) {
            String uninstrumented = instrumentationHelper.uninstrument(implementationReturn.getDescriptor());
            returnType = Type.getType(uninstrumented);
        } else {
            returnType = implementationReturn;
        }

        return new Descriptor(mergedParameters.toArray(new String[]{}), returnType.getDescriptor());
    }

    public boolean isInstanceCall() {
        return this.implementation.getOpcode() == Opcodes.INVOKEVIRTUAL || (this.implementation.getOpcode() == Opcodes.INVOKESPECIAL && !this.implementation.isConstructor()) || this.implementation.getOpcode() == Opcodes.INVOKEINTERFACE;
    }

    public String getProxyMethodName() {
        CombinedExcludedLookup lookup = new CombinedExcludedLookup();
        String name = "$fontus$" + Math.abs(this.hashCode()) + "$";
        if ("<init>".equals(this.implementation.getName())) {
            if (lookup.isPackageExcludedOrJdk(this.implementation.getOwner())) {
                name += this.implementation.getOwner().replace('/', '_') + "_$init$";
            } else {
                return this.implementation.getName();
            }
        } else {
            name += this.implementation.getName();
        }
        return name;
    }

    public int getProxyOpcodeTag() {
        return Opcodes.H_INVOKESTATIC;
//        if ("<init>".equals(this.implementation.getName())) {
//            return Opcodes.H_INVOKESPECIAL;
//        } else if ((this.implementation.getOpcode() & Opcodes.INVOKEVIRTUAL) != 0) {
//            return Opcodes.H_INVOKESTATIC;
//        } else {
//            return Opcodes.H_INVOKESTATIC;
//        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LambdaCall)) {
            return false;
        }
        LambdaCall call = (LambdaCall) o;
        return Objects.equals(this.functionalInterface, call.functionalInterface) && Objects.equals(this.implementation, call.implementation) && Objects.equals(this.invokeDescriptor, call.invokeDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.functionalInterface, this.implementation, this.invokeDescriptor);
    }

    public boolean isConstructorCall() {
        return this.implementationHandle.getTag() == Opcodes.H_NEWINVOKESPECIAL;
    }

    public boolean isStaticCall() {
        return this.implementation.getOpcode() == Opcodes.INVOKESTATIC;
    }

    public boolean isCallOnInterface() {
        return this.implementation.isInterface();
    }
}
