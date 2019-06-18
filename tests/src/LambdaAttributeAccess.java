import java.util.function.*;

class LambdaAttributeAccess {

    private Integer v;
    private Supplier<Integer> s;

    public LambdaAttributeAccess(int i) {
        this.v = i;
        this.s = () -> { return v+1; };
    }

    public int run() {
        return this.s.get();
    }

    public static void main(String[] args) {
        LambdaAttributeAccess laa = new LambdaAttributeAccess(5);
        System.out.println("RV: " + laa.run());

    }
}
