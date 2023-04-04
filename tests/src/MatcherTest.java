import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MatcherTest {

    public static void main(String[] args) {
        String url   = "http://regex.info/blog";
        String regex = "(?x) ^(https?):// ([^/:]+) (?:(\\d+))?";
        Pattern comma = Pattern.compile(regex);
        Matcher m = comma.matcher(url);
        while(m.find()) {
            MatchResult mr = m.toMatchResult();
            for (int i = 1; i <= mr.groupCount(); i++) {
                String match = mr.group(i);
                System.out.printf("%d : %s\n", i, match);
            }
        }
    }

}
