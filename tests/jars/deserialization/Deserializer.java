import java.io.*;
import java.util.HashMap;

class Deserializer {
    public Structure deserialize(String path) {
        try {

            ObjectInputStream is = new ObjectInputStream(new FileInputStream(path));
            Structure structure = (Structure) is.readObject();
            return structure;
        } catch(Exception ex) {
            System.err.printf("Exception: %s%n", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

}
