class StringAndBufferParamTest {

    public static boolean startsWith( StringBuffer buffer, String pattern ) {
        if (buffer.length() < pattern.length()) {
            return false;
        }
        else {
            for (int i = 0, len = pattern.length(); i < len; i++) {
                if (buffer.charAt(i) != pattern.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer("lorem ipsum blabla");
        String[] strs = { "blabla", "sup", "lorem" };
        for(String str : strs) {
            if(startsWith(buf, str)) {
                System.out.println(buf.toString() + " starts with: '" + str + "'");
            }
        }

    }

}
