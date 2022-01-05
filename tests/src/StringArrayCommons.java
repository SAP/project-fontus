class StringArrayCommons {

    static String[][] HTML40_EXTENDED_ESCAPE() { return HTML40_EXTENDED_ESCAPE.clone(); }
    private static final String[][] HTML40_EXTENDED_ESCAPE = {
        // <!-- Latin Extended-B -->
        {"\u0192", "&fnof;"}, // latin small f with hook = function= florin, U+0192 ISOtech -->
        // <!-- Greek -->
        {"\u0391", "&Alpha;"}, // greek capital letter alpha, U+0391 -->
        {"\u20AC", "&euro;"}, // -- euro sign, U+20AC NEW -->
    };
    public static void main(String[] args) {
        String[][] escape = HTML40_EXTENDED_ESCAPE();
        for(int i = 0; i < escape.length; i++) {
            String from = escape[i][0];
            String to = escape[i][1];
            System.out.printf("%s -> %s%n", from, to);
        }
    }
}
