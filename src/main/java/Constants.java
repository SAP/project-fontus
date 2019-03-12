import java.util.regex.Pattern;

final class Constants {
    static final Pattern strPattern = Pattern.compile("Ljava/lang/String\\b");
    static final String TString = "IASString";
    static final String TStringDesc = "LIASString";
    static final String TStringArrayDesc = "[LIASString;";

    private Constants() {}

}
