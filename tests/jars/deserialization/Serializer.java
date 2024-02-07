import java.io.*;
import java.util.HashMap;

public class Serializer {

    private static Structure makeStructure() {
        Structure s = new Structure();
        s.setValue("foobar");
        s.addValues("hey", "there");
        s.addValues("sup", "son?");
        s.addValues("KEK", 1337);
        return s;
    }

    public boolean serialize(String path) {
        try {
            Structure s = makeStructure();
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));
            os.writeObject(s);
            return true;
        } catch(Exception ex) {
            System.err.printf("Exception: %s%n", ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
}

