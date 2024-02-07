import java.util.*;

class ProcessBuilderTest {

    public static void main(String[] a) throws java.io.IOException, java.lang.InterruptedException {
        List<String> args = new ArrayList<>(5);
        args.add("java");
        args.add("-version");

        ProcessBuilder pb = new ProcessBuilder(args);

        Process p = pb.start();

        int ecode = p.waitFor();
        if(0 == ecode) {
            System.out.println("okay!");
        }

    }

}
