class StringSwitchTest {

    public String getGreeting(String language) {
        switch(language) {
            case "de":
                return "Hallo Welt";
            case "en":
                return "Hello World";
            case "www":
                return "Whats up?";
            default:
                return "Wrong language";

        }
    }

    public StringSwitchTest() {}

    public static void main(String[] args) {

        StringSwitchTest sst = new StringSwitchTest();
        String g1 = sst.getGreeting("de");
        System.out.println(g1);
        String g2 = sst.getGreeting("www");
        System.out.println(g2);
        String g3 = sst.getGreeting("en");
        System.out.println(g3);
        String g4 = sst.getGreeting("xxx");
        System.out.println(g4);
    }

}
