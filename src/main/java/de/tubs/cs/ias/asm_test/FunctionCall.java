package de.tubs.cs.ias.asm_test;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * Holds the information required for a 'invoke' bytecode instruction.
 */
@XmlRootElement
public class FunctionCall {
    public FunctionCall() { this.opcode = -1;
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
    @XmlElement
    private final boolean isInterface;

    FunctionCall(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isInterface = isInterface;
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        FunctionCall that = (FunctionCall) obj;
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
