class SystemGetProperty {
    public static final String FILE_SEPARATOR = getSystemProperty("file.separator");

    public static final String JAVA_FOO_BAR = getSystemProperty("java.foo.bar");

    private static String getSystemProperty(final String property) {
	try {
	    return System.getProperty(property);
	} catch (final SecurityException ex) {
	    // we are not allowed to look at this property
	    // System.err.println("Caught a SecurityException reading the system property '" + property
	    // + "'; the SystemUtils property value will default to null.");
	    return null;
	}
    }

    public static void main(String[] args) {

        System.out.println(FILE_SEPARATOR);
        System.out.println(JAVA_FOO_BAR);
    }
}
