
public class Example {


    public static void main(String[] args) {
        SimpleHTMLTagsFilter filter = new SimpleHTMLTagsFilter();
        String filtered = filter.filter("<div>hello&amp;</div><img src=x onerror=javascript:alert(1)");
        System.out.println(filtered);
    }

}
