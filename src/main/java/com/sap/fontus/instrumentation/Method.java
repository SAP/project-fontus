package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.utils.MethodUtils;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

public class Method {
    private final int access;
    private final String owner;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;
    private final boolean ownerIsInterface;

    public Method(int access, String owner, String internalName, String descriptor, String signature, String[] exceptions, boolean ownerIsInterface) {
        this.access = access;
        this.owner = owner;
        this.name = internalName;
        this.descriptor = descriptor;
        this.signature = signature;
        this.exceptions = exceptions;
        this.ownerIsInterface = ownerIsInterface;
    }

    public int getAccess() {
        return access;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public Descriptor getParsedDescriptor() {
        return Descriptor.parseDescriptor(descriptor);
    }

    public Type getDescriptorType() {
        return Type.getMethodType(descriptor);
    }

    public String getSignature() {
        return signature;
    }

    public String[] getExceptions() {
        return exceptions;
    }

    public String[] getParameterTypes() {
        return Descriptor.parseDescriptor(this.getDescriptor()).getParameters().toArray(new String[0]);
    }

    public boolean isDefault() {
        return ((this.access & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) ==
                Modifier.PUBLIC) && this.ownerIsInterface;
    }

    public static Method from(java.lang.reflect.Method method) {
        String signature = MethodUtils.getSignature(method).orElse(null);
        return new Method(method.getModifiers(), Utils.dotToSlash(method.getDeclaringClass().getName()), method.getName(), org.objectweb.asm.commons.Method.getMethod(method).getDescriptor(), signature, Arrays.stream(method.getExceptionTypes()).map(Class::getName).map(Utils::dotToSlash).toArray(String[]::new), method.getDeclaringClass().isInterface());
    }
}
