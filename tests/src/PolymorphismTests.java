import de.tubs.cs.ias.asm_test.TaintStringHelper;
class PolymorhismTests {
    public static void main(String[] args) {
        String s = new String("Hello world!");
        TaintStringHelper.setTaint(s, true);
        assert TaintStringHelper.isTainted();
    }
}