class StringValueOfNull {
    public static void main(String[] args) {
        Object foo = null;
        String vfoo = String.valueOf(foo);
        System.out.println(vfoo.toString());
    }
}
