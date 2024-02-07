class StringBuilderLocalVariableTest {

    static String append(String ...strs) {
        StringBuilder sb = new StringBuilder();
        for(String s : strs) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String output = append("hello", " ", "world", "!");
        System.out.println(output);
    }

}
