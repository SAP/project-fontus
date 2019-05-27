class StackOverflow {

    static void downToZero(int n) {
        if(n == 0) {
            return;
        }
        downToZero(n-1);
    }

    public static void main(String[] args) {
        int[] bounds = { 10, 100, 1000, 10000, 100000, 1000000 };
        for(int bound : bounds) {
            // The main wrapper method messes with stack depth..
            // So outputting it here breaks the test.
            //System.out.println("Testing for: " + bound);
            downToZero(bound);
        }
    }

}
