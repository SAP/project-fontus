class StringIndexOutOfBoundsExceptionTest {

    private String str;

    public StringIndexOutOfBoundsExceptionTest(String s) {
        this.str = s;
    }

    public String from(int idx) {
        if(idx >= this.str.length()) {
            throw new StringIndexOutOfBoundsException("idx : " + idx  + " out of bounds!");
        }
        return this.str.substring(idx);
    }

    public static void main(String[] args) {
        String input = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        StringIndexOutOfBoundsExceptionTest sioobet = new StringIndexOutOfBoundsExceptionTest(input);
        int[] from = {0, 10, 20, input.length()-2, input.length()+5 };
        for (int f : from) {
            try {
            String out = sioobet.from(f);
            System.out.println("From idx: " + f + ":\n" + out);
            } catch(StringIndexOutOfBoundsException se) {
                System.out.println("Exception: " + se.getMessage());
            }
        }
    }

}
