class Expression extends Base implements Xing, Printable {
    Expression() {
    }

    public String getSystemId() {
        return "SystemId(expr)";
    }

    public void print() {
        System.out.println("Expr");
    }
}

