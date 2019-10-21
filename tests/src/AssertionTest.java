class AssertionTest {

    static public void fail(String message) {
        if (message == null) {
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException(message);
    }

        static private void failNotEquals(String message, Object expected,
            Object actual) {
        fail(format(message, expected, actual));
    }

    static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if (message != null && !message.equals("")) {
            formatted = message + " ";
        }
        String expectedString = String.valueOf(expected);
        String actualString = String.valueOf(actual);
        if (expectedString.equals(actualString)) {
            return formatted + "expected: "
                    + formatClassAndValue(expected, expectedString)
                    + " but was: " + formatClassAndValue(actual, actualString);
        } else {
            return formatted + "expected:<" + expectedString + "> but was:<"
                    + actualString + ">";
        }
    }

    private static String formatClassAndValue(Object value, String valueString) {
        String className = value == null ? "null" : value.getClass().getName();
        return className + "<" + valueString + ">";
    }

    static public void assertEquals(String message, Object expected,
            Object actual) {
        if (equalsRegardingNull(expected, actual)) {
            return;
        } else if (expected instanceof String && actual instanceof String) {
            String cleanMessage = message == null ? "" : message;
            throw new IllegalArgumentException(String.format("%s: %s <-> %s", cleanMessage, (String) expected,
                    (String) actual));
        } else {
            failNotEquals(message, expected, actual);
        }
    }

    private static boolean equalsRegardingNull(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }

        return isEquals(expected, actual);
    }

    private static boolean isEquals(Object expected, Object actual) {
        return expected.equals(actual);
    }

    private static void assertEquals(Object lhs, Object rhs) {
        assertEquals(null, lhs, rhs);
    }

    public static void main(String[] args) {

        String str = new String("hello");

        StringBuilder sb = new StringBuilder(str);

        int a = (int) (Math.random() * sb.length());
        int b = (int) (Math.random() * sb.length());

        String s;
        if (a < b) {
            s = sb.subSequence(a, b).toString();
            assertEquals(str.subSequence(a, b), s);
        } else {
            s = sb.subSequence(b, a).toString();
            assertEquals(str.subSequence(b, a), s);
        }
        System.out.println("Okay");
    }
}
