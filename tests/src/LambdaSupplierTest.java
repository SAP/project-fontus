import java.util.function.Supplier;

class LambdaSupplierTest {
    public static void doesNotContain(String textToSearch, String substring, Supplier<String> messageSupplier) {
        if (hasLength(textToSearch) && hasLength(substring) && textToSearch.contains(substring)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static boolean hasLength(String str) {
        return (str != null && str.length() > 0);
    }

    private static String nullSafeGet(Supplier<String> messageSupplier) {
        return (messageSupplier != null ? messageSupplier.get() : null);
    }

    public static void doesNotContain(String textToSearch, String substring) {
        doesNotContain(textToSearch, substring,
                () -> "[Assertion failed] - this String argument must not contain the substring [" + substring + "]");
    }

    public static void main(String[] args) {
        LambdaSupplierTest.doesNotContain("hello my friend", "mate", () -> "String has to contain the substring!");
        LambdaSupplierTest.doesNotContain("hello my friend", "mate");
    }
}
