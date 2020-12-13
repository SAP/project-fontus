package de.tubs.cs.ias.asm_test.instrumentation;

import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.MethodUtils;
import de.tubs.cs.ias.asm_test.utils.Utils;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public class Method {
    private final int access;
    private final String owner;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;
    private final boolean ownerIsInterface;

    public Method(int access, String owner, String name, String descriptor, String signature, String[] exceptions, boolean ownerIsInterface) {
        this.access = access;
        this.owner = owner;
        this.name = name;
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
        String signature = null;
        if (MethodUtils.hasGenericInformation(method)) {
            signature = Descriptor.getSignature(method);
        }
        return new Method(method.getModifiers(), Utils.dotToSlash(method.getDeclaringClass().getName()), method.getName(), org.objectweb.asm.commons.Method.getMethod(method).getDescriptor(), signature, Arrays.stream(method.getExceptionTypes()).map(Class::getName).map(Utils::dotToSlash).toArray(String[]::new), method.getDeclaringClass().isInterface());
    }
}
