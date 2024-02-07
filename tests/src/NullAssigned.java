class NullAssigned {

    public static void print(String str) {
        if(str == null || str.isEmpty()) return;
        System.out.println("Hello : " + str);
    }

    public static void main(String[] args) {

        String s1 = "sup son?";
        print(s1);
        String s2 = null;
        print(s2);
    }

}
