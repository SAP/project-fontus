class NullReturnedFromStdLib {


    public static void print(String str) {
        if(str == null || str.isEmpty()) return;
        System.out.println("Hello : " + str);
    }

    public static void main(String[] args) {
        String s1 = System.getenv("TERM");
        print(s1);
        String s2 = System.getenv("doesnotexist!");
        print(s2);
    }

}
