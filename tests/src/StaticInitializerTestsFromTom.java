class StaticInitializerTestsFromTom {
    private static final String s;

    private static final String s2 = "I am a private static final string";

    private String s3 = "I am a private string";

    private static String s4 = "I am a private static string";

    private final String s5 = "I am a private final string";

    static {
        s = new String("I am a static final string dynamically initialized!");
    }

    String getS() { return StaticInitializerTestsFromTom.s; }
    String getS2() { return StaticInitializerTestsFromTom.s2; }
    String getS3() { return s3; }
    String getS4() { return StaticInitializerTestsFromTom.s4; }
    String getS5() { return s5; }

    public static void main(String[] args) {
        StaticInitializerTestsFromTom sitft = new StaticInitializerTestsFromTom();
        String s = sitft.getS();
        String s2 = sitft.getS2();
        String s3 = sitft.getS3();
        String s4 = sitft.getS4();
        String s5 = sitft.getS5();
        System.out.println("s1 = " + s);
        System.out.println("s2 = " + s2);
        System.out.println("s3 = " + s3);
        System.out.println("s4 = " + s4);
        System.out.println("s5 = " + s5);
    }
}
