import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.util.logging.LogManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("foo");
        System.setProperty("java.util.logging.config.file", "foo");
        System.out.println("foo2");
        Log log = LogFactory.getLog(Main.class);
        System.out.println(LogManager.getLogManager().getProperty(".level"));
        log.error("Hello World!");
    }

}
