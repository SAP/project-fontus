import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ValueOfTest {

    private final List<Object> values = new LinkedList<>();

    public void add(Object val) {
        this.values.add(val);
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> getStringValues() {
        List<String> stringList = new ArrayList<>(this.values.size());
        for (Object value : this.values) {
            stringList.add(value.toString());
        }
        return Collections.unmodifiableList(stringList);
    }

    public Object getValue() {
        return (!this.values.isEmpty() ? this.values.get(0) : null);
    }

    public String getStringValue() {
        return (!this.values.isEmpty() ? String.valueOf(this.values.get(0)) : null);
    }

    public static void main(String[] args) {

        ValueOfTest vot = new ValueOfTest();

        String v0 = vot.getStringValue();
        System.out.println("v0 = " + v0);

        vot.add("sup");
        String v1 = vot.getStringValue();
        System.out.println("v1 = " + v1);
        vot.add("son");
        vot.add("?");
        v1 = vot.getStringValue();
        System.out.println("v1 = " + v1);

        List<String> vls = vot.getStringValues();
        int i = 1;
        for(String vl : vls) {
            System.out.println("v" + i + " = " + vl);
            i++;
        }
    }
}
