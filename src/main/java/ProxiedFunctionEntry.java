import java.util.Objects;

public class ProxiedFunctionEntry {
    private final String owner;
    private final String name;
    private final String descriptor;

    ProxiedFunctionEntry(String owner, String name, String descriptor) {
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        ProxiedFunctionEntry that = (ProxiedFunctionEntry) obj;
        return this.owner.equals(that.owner) &&
                this.name.equals(that.name) &&
                this.descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.owner, this.name, this.descriptor);
    }
}
