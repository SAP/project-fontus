import java.io.PrintStream;

class PrintStreamProxies {

    public static void println(PrintStream stream, IASString str) {
        stream.println(str.getString());
    }
}
