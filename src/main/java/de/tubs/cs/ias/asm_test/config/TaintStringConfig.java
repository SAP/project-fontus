package de.tubs.cs.ias.asm_test.config;

import static de.tubs.cs.ias.asm_test.Constants.StringDesc;

public class TaintStringConfig {
    /**
     * The package our taint-aware classes are in
     */
    private final String TPackage;

    /**
     * The fully qualified name type of our taint-aware String
     */
    private final String TStringQN;

    /**
     * The fully qualified name type of our taint-aware StringBuffer
     */
    private final String TStringBufferQN;

    private final String TStringUtilsQN;

    /**
     * The fully qualified name of the class containing the Reflection proxies.
     */
    private final String ReflectionProxiesQN;

    /**
     * The bytecode descriptor of our taint aware string
     */
    private final String TStringDesc;
    /**
     * The bytecode descriptor of an array of our taint aware string
     */
    private final String TStringArrayDesc;

    /**
     * The type of our taint-aware StringBuilder
     */
    private final String TStringBuilderQN;

    /**
     * The bytecode descriptor of our taint aware StringBuilder
     */
    private final String TStringBuilderDesc;
    /**
     * The bytecode descriptor of our taint aware StringBuffer
     */
    private final String TStringBufferDesc;

    /**
     * Descriptor of the concat method
     */
    private final String ConcatDesc;

    /**
     * Descriptor of the instrumented toString method.
     */
    private final String ToStringInstrumentedDesc;

    /**
     * Descriptor of the 'tainted' method that turns a regular String into a tainted one
     */
    private final String CreateTaintedStringDesc;
    private final String AS_STRING_DESC;
    private final String FROM_STRING_DESC;
    private final String TFormatterDesc;
    private final String TFormatterQN;
    private final String TMatcherDesc;
    private final String TMatcherQN;
    private final String TPatternDesc;
    private final String TPatternQN;

    public String getTPackage() {
        return TPackage;
    }

    public String getTStringQN() {
        return TStringQN;
    }

    public String getTStringBufferQN() {
        return TStringBufferQN;
    }

    public String getTStringUtilsQN() {
        return TStringUtilsQN;
    }

    public String getReflectionProxiesQN() {
        return ReflectionProxiesQN;
    }

    public String getTStringDesc() {
        return TStringDesc;
    }

    public String getTStringArrayDesc() {
        return TStringArrayDesc;
    }

    public String getTStringBuilderQN() {
        return TStringBuilderQN;
    }

    public String getTStringBuilderDesc() {
        return TStringBuilderDesc;
    }

    public String getTStringBufferDesc() {
        return TStringBufferDesc;
    }

    public String getConcatDesc() {
        return ConcatDesc;
    }

    public String getToStringInstrumentedDesc() {
        return ToStringInstrumentedDesc;
    }

    public String getCreateTaintedStringDesc() {
        return CreateTaintedStringDesc;
    }

    public String getAS_STRING_DESC() {
        return AS_STRING_DESC;
    }

    public String getFROM_STRING_DESC() {
        return FROM_STRING_DESC;
    }

    private final TaintMethod taintMethod;

    public TaintStringConfig(TaintMethod taintMethod) {
        this.taintMethod = taintMethod;
        this.TPackage = "de/tubs/cs/ias/asm_test/taintaware/" + taintMethod.getSubPath();
        this.TStringQN = TPackage + "IASString";
        this.TStringBufferQN = TPackage + "IASStringBuffer";
        this.TStringUtilsQN = TPackage + "IASStringUtils";
        this.ReflectionProxiesQN = TPackage + "IASReflectionProxies";
        this.TStringDesc = java.lang.String.format("L%s;", TStringQN);
        this.TStringArrayDesc = "[" + TStringDesc;
        this.TStringBuilderQN = TPackage + "IASStringBuilder";
        this.TStringBuilderDesc = String.format("L%s;", TStringBuilderQN);
        this.TStringBufferDesc = String.format("L%s;", TStringBufferQN);
        this.ConcatDesc = String.format("(%s[Ljava/lang/Object;)%s", StringDesc, TStringDesc);
        this.ToStringInstrumentedDesc = String.format("()%s", TStringDesc);
        this.CreateTaintedStringDesc = String.format("(Ljava/lang/String;)%s", TStringDesc);
        this.AS_STRING_DESC = String.format("(%s)%s", TStringDesc, StringDesc);
        this.FROM_STRING_DESC = String.format("(%s)%s", StringDesc, TStringDesc);
        this.TFormatterQN = TPackage + "IASFormatter";
        this.TFormatterDesc = java.lang.String.format("L%s;", TFormatterQN);
        this.TMatcherQN = TPackage + "IASMatcher";
        this.TMatcherDesc = java.lang.String.format("L%s;", TMatcherQN);
        this.TPatternQN = TPackage + "IASPattern";
        this.TPatternDesc = java.lang.String.format("L%s;", TPatternQN);
    }

    public String getTFormatterDesc() {
        return this.TFormatterDesc;
    }

    public String getTFormatterQN() {
        return this.TFormatterQN;
    }

    public TaintMethod getTaintMethod() {
        return this.taintMethod;
    }

    public String getTMatcherDesc() {
        return this.TMatcherDesc;
    }

    public String getTMatcherQN() {
        return this.TMatcherQN;
    }

    public String getTPatternDesc() {
        return this.TPatternDesc;
    }

    public String getTPatternQN() {
        return this.TPatternQN;
    }
}
