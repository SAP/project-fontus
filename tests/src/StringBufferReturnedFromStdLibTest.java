import java.io.PrintWriter;
import java.io.StringWriter;

class StringBufferReturnedFromStdLibTest {

    private final Throwable t;

    private final String testClass;

    private final String testMethod;

    public StringBufferReturnedFromStdLibTest(String testClass, String testMethod, Throwable t)
    {
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.t = t;
    }

    public String writeTraceToString()
    {
        if ( t != null )
        {
            StringWriter w = new StringWriter();
            try ( PrintWriter stackTrace = new PrintWriter( w ) )
            {
                t.printStackTrace( stackTrace );
            }
            StringBuffer builder = w.getBuffer();
            String exc = t.getClass().getName() + ": ";
            if ( builder.toString().startsWith(exc))
            {
                builder.insert( exc.length(), '\n' );
            }
            return builder.toString();
        }
        return "";
    }

    private static void throwIt() {
        throw new IllegalArgumentException("sup");
    }

    public static void main(String[] args) {
        try {
            throwIt();
        } catch (Exception exc) {
            StringBufferReturnedFromStdLibTest test = new StringBufferReturnedFromStdLibTest("Testing", "test()", exc);
            String s = test.writeTraceToString();
            String[] lines = s.split("\n");
            for(String line : lines) {
                if(!line.contains("main")) {
                    System.out.println(line);
                }
            }
        }
    }
}
