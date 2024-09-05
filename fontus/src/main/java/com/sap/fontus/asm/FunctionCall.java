package com.sap.fontus.asm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.fontus.Constants;
import com.sap.fontus.utils.MethodUtils;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Holds the information required for an 'invoke' bytecode instruction.
 */
@XmlRootElement
public class FunctionCall {
    public FunctionCall() {
        this.opcode = -1;
        this.owner = "";
        this.name = "";
        this.descriptor = "";
        this.parsedDescriptor = null;
        this.isInterface = false;
    }

    public static final FunctionCall EmptyFunctionCall = new FunctionCall();

    @XmlElement
    private final int opcode;
    @XmlElement
    private final String owner;
    @XmlElement
    private final String name;
    @XmlElement
    private final String descriptor;

    @XmlElement(name = "interface")
    @JsonProperty("interface")
    private final boolean isInterface;

    @JsonIgnore
    private Descriptor parsedDescriptor;

    public FunctionCall(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.parsedDescriptor = Descriptor.parseDescriptor(descriptor);
        this.isInterface = isInterface;
    }

    public static FunctionCall fromMethod(Method method) {
        int opcode;
        if (Modifier.isStatic(method.getModifiers())) {
            opcode = Opcodes.INVOKESTATIC;
        } else if (method.getDeclaringClass().isInterface()) {
            opcode = Opcodes.INVOKEINTERFACE;
        } else if (Modifier.isPrivate(method.getModifiers())) {
            opcode = Opcodes.INVOKESPECIAL;
        } else {
            opcode = Opcodes.INVOKEVIRTUAL;
        }
        String descriptor = Type.getType(method).getDescriptor();
        return new FunctionCall(opcode, Utils.dotToSlash(method.getDeclaringClass().getName()), method.getName(), descriptor, method.getDeclaringClass().isInterface());
    }

    private static Class<?> getClazz(String s) throws ClassNotFoundException {
        char c = s.charAt(0);
        switch (c) {
            case 'V':
                return Void.TYPE;
            case 'Z':
                return Boolean.TYPE;
            case 'C':
                return Character.TYPE;
            case 'B':
                return Byte.TYPE;
            case 'S':
                return Short.TYPE;
            case 'I':
                return Integer.TYPE;
            case 'F':
                return Float.TYPE;
            case 'J':
                return Long.TYPE;
            case 'D':
                return Double.TYPE;
            case '[':
                return Array.newInstance(getClazz(s.substring(1)), 0).getClass();
            case 'L':
                return Class.forName(Utils.slashToDot(s.substring(1, s.length() - 1)));
            default:
                return Class.forName(Utils.slashToDot(s));
        }
    }

    public static Method toMethod(FunctionCall functionCall) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = Class.forName(Utils.slashToDot(functionCall.owner));
        String[] params = functionCall.getParsedDescriptor().getParameters();
        Class<?>[] paramClasses = new Class[params.length];
        int i = 0;
        for (String s : params) {
            paramClasses[i] = getClazz(s);
            i++;
        }

        return clazz.getMethod(functionCall.name, paramClasses);
    }

    public static FunctionCall fromHandle(Handle handle) {
        return new FunctionCall(MethodUtils.tagToOpcode(handle.getTag()), handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface());
    }

    public static Handle toHandle(FunctionCall fc) {
        return new Handle(MethodUtils.opCodeToTag(fc.opcode), fc.owner, fc.name, fc.descriptor, fc.isInterface);
    }

    public static FunctionCall fromConstructor(Constructor<?> constructor) {
        String descriptor = Type.getType(constructor).getDescriptor();
        return new FunctionCall(Opcodes.INVOKESPECIAL, Utils.dotToSlash(constructor.getDeclaringClass().getName()), Constants.Init, descriptor, constructor.getDeclaringClass().isInterface());
    }

    public String getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public String getFqn() {
        return this.owner + "." + this.name + this.descriptor;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public Descriptor getParsedDescriptor() {
        if (this.parsedDescriptor == null) {
            this.parsedDescriptor = Descriptor.parseDescriptor(this.descriptor);
        }
        return this.parsedDescriptor;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public boolean isInterface() {
        return this.isInterface;
    }

    @JsonIgnore
    public boolean isInstanceMethod() {
        return this.opcode == Opcodes.INVOKESPECIAL || this.opcode == Opcodes.INVOKEVIRTUAL || this.opcode == Opcodes.INVOKEINTERFACE;
    }

    @JsonIgnore
    public boolean isRelevantMethodHandleInvocation() {
        return "java/lang/invoke/MethodHandle".equals(this.owner) && (
                "invoke".equals(this.name) ||
                        "invokeExact".equals(this.name) ||
                        "invokeWithArguments".equals(this.name));
    }

    @JsonIgnore
    public boolean isVirtualOrStaticMethodHandleLookup() {
        return "java/lang/invoke/MethodHandles$Lookup".equals(this.owner) && (
                "findVirtual".equals(this.name) ||
                        "findStatic".equals(this.name)
        );
    }

    @JsonIgnore
    public boolean isSpecialMethodHandleLookup() {
        return "java/lang/invoke/MethodHandles$Lookup".equals(this.owner) && "findSpecial".equals(this.name);
    }

    @JsonIgnore
    public boolean isConstructorMethodHandleLookup() {
        return "java/lang/invoke/MethodHandles$Lookup".equals(this.owner) && "findConstructor".equals(this.name);
    }

    @JsonIgnore
    public boolean isConstructor() {
        return this.opcode == Opcodes.INVOKESPECIAL && "<init>".equals(this.name);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.equals(EmptyFunctionCall);
    }

    @Override
    public String toString() {
        return "FunctionCall{" +
                "opcode=" + this.opcode +
                ", owner='" + this.owner + '\'' +
                ", name='" + this.name + '\'' +
                ", descriptor='" + this.descriptor + '\'' +
                ", isInterface=" + this.isInterface +
                '}';
    }

    /**
     * Match the method class, name and descriptor, but not the opcode and interface flag
     * Means that the opcode and interface flags don't need to exactly match
     * @param obj Object to be matched
     * @return are the function calls similar enough?
     */
    public boolean fuzzyEquals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final FunctionCall that = (FunctionCall) obj;
        return this.owner.equals(that.owner) &&
               this.name.equals(that.name) &&
               this.descriptor.equals(that.descriptor);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final FunctionCall that = (FunctionCall) obj;
        return this.opcode == that.opcode &&
                this.isInterface == that.isInterface &&
                this.owner.equals(that.owner) &&
                this.name.equals(that.name) &&
                this.descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.opcode, this.owner, this.name, this.descriptor, this.isInterface);
    }
}
