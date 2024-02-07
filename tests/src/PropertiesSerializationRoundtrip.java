import java.util.Properties;
import java.io.*;

class PropertiesSerializationRoundtrip {


    private static byte[] serialize(Properties props) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(props);
        os.close();
        return baos.toByteArray();
    }

    private static Properties deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(bais);
        return (Properties) is.readObject();
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("Sup", "Son");
        props.put("hello", "there");
        byte[] bytes = serialize(props);
        Properties props2 = deserialize(bytes);
        System.out.printf("Sizes: %d <-> %d%n", props.size(), props2.size());
        for(Object key : props.keySet()) {
            Object v1 = props.get(key);
            Object v2 = props2.get(key);
            System.out.printf("%s (%s) <-> %s (%s)%n", v1, v1.getClass().getName(), v2, v2.getClass().getName());
        }
        for(Object key : props2.keySet()) {
            Object v1 = props.get(key);
            Object v2 = props2.get(key);
            System.out.printf("%s (%s) <-> %s (%s)%n", v1, v1.getClass().getName(), v2, v2.getClass().getName());
        }

    }
}
