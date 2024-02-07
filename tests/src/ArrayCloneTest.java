class ArrayCloneTest {

    public static void main(String[] args) {
        String[] args2 = (String[]) args.clone();
        for(String a : args2) {
            System.out.println(a);
        }
    }
}
