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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Holds the information required for a 'invoke' bytecode instruction.
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
    @JsonProperty(value = "interface")
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

    public static FunctionCall fromHandle(Handle handle) {
        return new FunctionCall(MethodUtils.tagToOpcode(handle.getTag()), handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface());
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
        return getOwner() + "." + getName() + getDescriptor();
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
        return this.getOpcode() == Opcodes.INVOKESPECIAL || this.getOpcode() == Opcodes.INVOKEVIRTUAL || this.getOpcode() == Opcodes.INVOKEINTERFACE;
    }

    @JsonIgnore
    public boolean isConstructor() {
        return this.getOpcode() == Opcodes.INVOKESPECIAL && "<init>".equals(this.getName());
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.equals(EmptyFunctionCall);
    }

    @Override
    public String toString() {
        return "FunctionCall{" +
                "opcode=" + opcode +
                ", owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", isInterface=" + isInterface +
                '}';
    }

    /**
     * Match the method class, name and descriptor, but not the opcode and interface flag
     * Means that the opcode and interface flags don't need to exactly match
     * @param obj Object to be matched
     * @return
     */
    public boolean fuzzyEquals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        final FunctionCall that = (FunctionCall) obj;
        return this.owner.equals(that.owner) &&
               this.name.equals(that.name) &&
               this.descriptor.equals(that.descriptor);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;
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
