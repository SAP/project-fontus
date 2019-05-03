import java.util.regex.Pattern;

final class Constants {
    static final Pattern strPattern = Pattern.compile("Ljava/lang/String\\b");
    /**
     * The type of our taint-aware String
     */
    static final String TString = "IASString";
    /**
     * The bytecode descriptor of our taint aware string
     */
    static final String TStringDesc = "LIASString";
    /**
     * The bytecode descriptor of an array of our taint aware string
     */
    static final String TStringArrayDesc = "[LIASString;";

    private Constants() {}

}
