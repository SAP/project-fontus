class ReflectionTestConstructor {

    public static void main(String[] args) throws Exception {
        Class strClazz = Class.forName("java.lang.String");
        String str  = (String) strClazz.getConstructor(strClazz).newInstance("world");
        str = "hello"+ " " + str;
        System.out.println(str);
    }

}
