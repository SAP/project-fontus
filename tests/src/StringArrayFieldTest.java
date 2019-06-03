class StringArrayFieldTest {

    String[] words;

    public StringArrayFieldTest(String[] w) {
        this.words = w;
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        for(String w : this.words) {
            sb.append(w);
        }
        return sb.toString();
    }
    public static void main(String[] args) {
        String[] words = { "hello", " ", "world", "!" };
        StringArrayFieldTest sat = new StringArrayFieldTest(words);
        System.out.println(sat.format());
    }
}

