package de.tubs.cs.ias.asm_test.asm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.utils.Utils;
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
        this.isInterface = false;
    }

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

    public FunctionCall(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isInterface = isInterface;
    }

    public static FunctionCall fromMethod(Method method) {
        int opcode;
        if (Modifier.isStatic(method.getModifiers())) {
            opcode = Opcodes.INVOKESTATIC;
        } else if (method.getDeclaringClass().isInterface()) {
            opcode = Opcodes.INVOKEINTERFACE;
        } else if( Modifier.isPrivate(method.getModifiers())) {
            opcode = Opcodes.INVOKESPECIAL;
        } else {
            opcode = Opcodes.INVOKEVIRTUAL;
        }
        String descriptor = Type.getType(method).getDescriptor();
        return new FunctionCall(opcode, Utils.fixupReverse(method.getDeclaringClass().getName()), method.getName(), descriptor, method.getDeclaringClass().isInterface());
    }

    public static FunctionCall fromConstructor(Constructor<?> constructor) {
        String descriptor = Type.getType(constructor).getDescriptor();
        return new FunctionCall(Opcodes.INVOKESPECIAL, Utils.fixupReverse(constructor.getDeclaringClass().getName()), Constants.Init, descriptor, constructor.getDeclaringClass().isInterface());
    }

    public String getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public boolean isInterface() {
        return this.isInterface;
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
