class ArrayCopyTest {
    public static void main(String[] args) {

        String[] src = {"hello", "world"};
        String[] target = new String[src.length];
        System.arraycopy(src, 0, target, 0, src.length);
        for(String t : target) {
            System.out.println(t);
        }

    }
}
