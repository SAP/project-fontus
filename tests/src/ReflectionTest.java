class ReflectionTest {

    public static void main(String[] args) throws Exception {
        String str  = (String) Class.forName("java.lang.String").getConstructor().newInstance();
        str = str + "hello";
        System.out.println(str);
    }

}
