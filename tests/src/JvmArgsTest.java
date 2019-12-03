import java.lang.management.ManagementFactory;
import java.util.*;

class JvmArgsTest {

    public static void main(String[] args) {
        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        String[] argsArray = inputArguments.toArray(new String[inputArguments.size()]);
        for(String arg : argsArray) {
            System.out.println(arg);
        }
    }

}
