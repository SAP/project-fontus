import java.util.Objects;

public class BlackListEntry {
    private final String name;
    private final String descriptor;
    private final int access;

    public BlackListEntry(String name, String descriptor, int access) {
        this.name = name;
        this.descriptor = descriptor;
        this.access = access;
    }

    public boolean matches(String name, String descriptor, int access) {
        return this.name.equals(name) && this.descriptor.equals(descriptor) && this.access == access;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        BlackListEntry that = (BlackListEntry) obj;
        return this.access == that.access &&
                this.name.equals(that.name) &&
                this.descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.descriptor, this.access);
    }
}
