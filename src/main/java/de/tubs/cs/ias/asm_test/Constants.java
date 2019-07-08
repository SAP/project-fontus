package de.tubs.cs.ias.asm_test;

import java.util.regex.Pattern;

final class Constants {
    /**
     * The package our taint-aware classes are in
     */
    private static final String TPackage = "de/tubs/cs/ias/asm_test/";

    /**
     * Fully qualified name of the java Object class.
     */
    static final String ObjectQN = "java/lang/Object";
    /**
     * The fully qualified name of the String class
     */
    static final String StringQN = "java/lang/String";
    /**
     * The fully qualified name of the StringBuilder class
     */
    static final String StringBuilderQN = "java/lang/StringBuilder";
    /**
     * The fully qualified name type of our taint-aware String
     */
    static final String TStringQN = TPackage + "IASString";
    /**
     * Name of the toString method.
     */
    static final String ToString = "toString";

    /**
     * Descriptor of the java Object class
     */
    static final String ObjectDesc = java.lang.String.format("L%s;", ObjectQN);


    static final String ReflectionProxiesQN = TPackage + "IASReflectionProxies";

    /**
     * The bytecode descriptor of our taint aware string
     */
    static final String TStringDesc = java.lang.String.format("L%s;", TStringQN);
    /**
     * Descriptor of the java String class
     */
    static final String StringDesc = java.lang.String.format("L%s;", StringQN);
    /**
     * Descriptor of the java StringBuilder class
     */
    static final String StringBuilderDesc = java.lang.String.format("L%s;", StringBuilderQN);
    /**
     * The bytecode descriptor of an array of our taint aware string
     */
    static final String TStringArrayDesc = "[" + TStringDesc;
    /**
     * The type of our taint-aware StringBuilder
     */
    static final String TStringBuilderQN = TPackage + "IASStringBuilder";
    /**
     * The bytecode descriptor of our taint aware StringBuilder
     */
    static final String TStringBuilderDesc = String.format("L%s;", TStringBuilderQN);

    /**
     * Constructor name
     */
    static final String Init = "<init>";

    /**
     * Static initializer name
     */
    static final String ClInit = "<clinit>";

    /**
     * Autogenerated name of the main wrapper function
     */
    static final String MainWrapper = "$main";

    /**
     * Name of the instrumented toString method
     */
    static final String ToStringInstrumented = "$toString";

    /**
     * Name of the method that converts taint-aware Strings to regular ones
     */
    static final String TStringToStringName = "getString";
    /**
     * Descriptor of an object to regular String conversion method
     */
    static final String ToStringDesc = "()Ljava/lang/String;";
    /**
     * Descriptor of the untainted init/constructor method.
     */
    static final String TStringInitUntaintedDesc = "(Ljava/lang/String;)V";

    /**
     * Descriptor of the concat method
     */
    static final String ConcatDesc = String.format("(%s[Ljava/lang/Object;)%s", StringDesc, TStringDesc);

    /**
     * Descriptor of the instrumented toString method.
     */
    static final String ToStringInstrumentedDesc = String.format("()%s", TStringDesc);

    /**
     * Descriptor of the 'tainted' method that turns a regular String into a tainted one
     */
    static final String CreateTaintedStringDesc = String.format("(Ljava/lang/String;)%s", TStringDesc);

    /**
     * Matches fully qualified String names
     */
    static final Pattern strPattern = Pattern.compile(StringDesc);
    /**
     * Matches fully qualified StringBuilder names
     */
    static final Pattern strBuilderPattern = Pattern.compile(StringBuilderDesc);
    static final String ABORT_IF_TAINTED = "abortIfTainted";


    private Constants() {}

}
