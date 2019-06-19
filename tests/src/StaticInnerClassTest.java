class StaticInnerClassTest {
    static String data = "world";
    static class Inner{
        void msg(){
            System.out.println("hello "+data);
        }
    }
    public static void main(String args[]){
        StaticInnerClassTest.Inner obj=new StaticInnerClassTest.Inner();
        obj.msg();
    }
}
