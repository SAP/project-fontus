class MultipleStaticInitializers {

    private final static String t1;
    static {
        t1 = "Hello";
    }

    private final static String t2;
    static {
        t2 = "World";
    }

    public MultipleStaticInitializers() { }

    public String getGreeting() {
        return MultipleStaticInitializers.t1 + " " + MultipleStaticInitializers.t2;
    }

    public static void main(String[] args) {
        MultipleStaticInitializers sit = new MultipleStaticInitializers();
        String eg = sit.getGreeting();
        System.out.println(eg);
    }
}
