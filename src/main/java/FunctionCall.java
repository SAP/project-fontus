import java.util.Objects;

/**
 * Holds the information required for a 'invoke' bytecode instruction.
 */
public class FunctionCall {
    private final int opcode;
    private final String owner;
    private final String name;
    private final String descriptor;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        FunctionCall that = (FunctionCall) o;
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
