class StringArrayTest {

    public static void main(String[] args) {
        String[] words = { "hello", " ", "world", "!" };
        StringBuilder sb = new StringBuilder();
        for(String w : words) {
            sb.append(w);
        }
        System.out.println(sb.toString());
    }
}
