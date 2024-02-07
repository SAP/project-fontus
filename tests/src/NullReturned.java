class NullReturned {

    public static String getString(int v) {
        if(v == 0) {
            return null;
        } else {
            return "sup";
        }
    }

    public static void print(String str) {
        if(str == null || str.isEmpty()) return;
        System.out.println("Hello : " + str);
    }
    public static void main(String[] args) {
        String s1 = getString(1);
        print(s1);
        String s2 = getString(0);
        print(s2);
    }
}
