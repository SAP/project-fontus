@Descriptor(
    value="123",
    name="Jakob",
    age=37,
    newNames={"Jenkov", "Peterson"}
)
public class Entity {

    private final String name;
    public Entity(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
