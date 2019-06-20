class ReflectionDirect {


    public static void main(String[] args) throws Exception {
        Class strClazz = String.class;
        String str  = (String) strClazz.getConstructor(strClazz).newInstance("world");
        str = "hello"+ " " + str;
        System.out.println(str);
    }
}
