import java.util.HashMap;
import java.io.Serializable;

class Structure implements Serializable {
    private static final long serialVersionUID = 42L;
    private String value;
    private HashMap<String, Object> values;
    private String[] vals;
    private HashMap<String, String[]> valsMap;
    Structure() {
        this.value = "foo";
        this.values = new HashMap<>();
        this.valsMap = new HashMap<>();
        this.vals = new String[] { "a", "b", "c" };
        this.valsMap.put("a", new String[] {"a" });
    }

    @Override
    public String toString() {
            return "Structure{" +
                    "value='" + value + '\'' +
                    ", vals=[" + vals + "]" +
                    ", values=" + values +
                    '}';
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void addValues(String k, Object v) {
        this.values.put(k, v);
    }
    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    public String[] getVals() {
        return this.vals;
    }

    public void setVals(String[] vals) {
        this.vals = vals;
    }
}
