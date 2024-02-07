import org.unbescape.html.*;

public class Main {

    public static void main(String[] args) {
        // Text is an instance of IASString
        String text = "/js/libs/modernizr-2.5.3.min.js";
        HtmlEscapeType type = HtmlEscapeType.HTML4_NAMED_REFERENCES_DEFAULT_TO_DECIMAL;
        HtmlEscapeLevel level = HtmlEscapeLevel.LEVEL_1_ONLY_MARKUP_SIGNIFICANT;
        String escaped = HtmlEscape.escapeHtml(text, type, level);
        System.out.println(escaped);
    }
}
