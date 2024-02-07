import java.util.Map;
import java.util.HashMap;

class MapAppendLambdaTest {

    private Map<String, String> parameters;

    public MapAppendLambdaTest() {
        this.parameters = new HashMap<>();
    }

    public void add(String key, String val) {
        this.parameters.put(key, val);
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        appendTo(this.parameters, sb);
        return sb.toString();
    }
    private static void appendTo(Map<String, String> map, StringBuilder builder) {
        map.forEach((key, val) -> {
        builder.append(';');
        builder.append(key);
        builder.append('=');
        builder.append(val);
        });
    }
    public static void main(String[] args) {
        MapAppendLambdaTest malt = new MapAppendLambdaTest();
        malt.add("p1", "v1");
        malt.add("p2", "v2");
        malt.add("p3", "v3");
        malt.add("p4", "v4");
        String out = malt.asString();
        System.out.println("Parameters" + out);
    }
}
