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

    private final String MethodTStringDesc;
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
    private final String MethodTStringBuilderDesc;
    /**
     * The bytecode descriptor of our taint aware StringBuffer
     */
    private final String TStringBufferDesc;

    private final String MethodTStringBufferDesc;

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
    private final String asStringDesc;
    private final String fromStringDesc;
    private final String TFormatterDesc;
    private final String MethodTFormatterDesc;
    private final String TFormatterQN;
    private final String TMatcherDesc;
    private final String TMatcherQN;
    private final String TPatternDesc;
    private final String TPatternQN;
    private final String ReflectionMethodProxyQN;
    private final String ToArrayProxyQN;

    public String getTPackage() {
        return this.TPackage;
    }

    public String getTStringQN() {
        return this.TStringQN;
    }

    public String getTStringBufferQN() {
        return this.TStringBufferQN;
    }

    public String getTStringUtilsQN() {
        return this.TStringUtilsQN;
    }

    public String getReflectionProxiesQN() {
        return this.ReflectionProxiesQN;
    }

    public String getTStringDesc() {
        return this.TStringDesc;
    }

    public String getTStringArrayDesc() {
        return this.TStringArrayDesc;
    }

    public String getTStringBuilderQN() {
        return this.TStringBuilderQN;
    }

    public String getTStringBuilderDesc() {
        return this.TStringBuilderDesc;
    }

    public String getTStringBufferDesc() {
        return this.TStringBufferDesc;
    }

    public String getConcatDesc() {
        return this.ConcatDesc;
    }

    public String getToStringInstrumentedDesc() {
        return this.ToStringInstrumentedDesc;
    }

    public String getCreateTaintedStringDesc() {
        return this.CreateTaintedStringDesc;
    }

    public String getAsStringDesc() {
        return this.asStringDesc;
    }

    public String getFromStringDesc() {
        return this.fromStringDesc;
    }

    private final TaintMethod taintMethod;

    public TaintStringConfig(TaintMethod taintMethod) {
        this.taintMethod = taintMethod;
        String taintawarePackage = "de/tubs/cs/ias/asm_test/taintaware/";
        String sharedPackage = taintawarePackage + "shared/";
        this.TPackage = taintawarePackage + taintMethod.getSubPath();
        this.TStringQN = this.TPackage + "IASString";
        this.TStringBufferQN = this.TPackage + "IASStringBuffer";
        this.TStringUtilsQN = sharedPackage + "IASStringUtils";
        this.ReflectionProxiesQN = sharedPackage + "IASReflectionProxies";
        this.ReflectionMethodProxyQN = sharedPackage + "IASReflectionMethodProxy";
        this.ToArrayProxyQN = sharedPackage + "IASToArrayProxy";
        this.TStringDesc = String.format("L%s;", this.TStringQN);
        this.MethodTStringDesc = String.format("L%sIASStringable;", sharedPackage);
        this.TStringArrayDesc = "[" + this.TStringDesc;
        this.TStringBuilderQN = this.TPackage + "IASStringBuilder";
        this.TStringBuilderDesc = String.format("L%s;", this.TStringBuilderQN);
        this.MethodTStringBuilderDesc = "Lde/tubs/cs/ias/asm_test/taintaware/shared/IASStringBuilderable;";
        this.TStringBufferDesc = String.format("L%s;", this.TStringBufferQN);
        this.MethodTStringBufferDesc = "Lde/tubs/cs/ias/asm_test/taintaware/shared/IASStringBuilderable;";
        this.ConcatDesc = String.format("(%s[Ljava/lang/Object;)%s", StringDesc, this.TStringDesc);
        this.ToStringInstrumentedDesc = String.format("()%s", this.TStringDesc);
        this.CreateTaintedStringDesc = String.format("(%s)%s", this.TStringDesc, this.TStringDesc);
        this.asStringDesc = String.format("(%s)%s", this.TStringDesc, StringDesc);
        this.fromStringDesc = String.format("(%s)%s", StringDesc, this.TStringDesc);
        this.TFormatterQN = this.TPackage + "IASFormatter";
        this.TFormatterDesc = String.format("L%s;", this.TFormatterQN);
        this.MethodTFormatterDesc = "Lde/tubs/cs/ias/asm_test/taintaware/shared/IASFormatterable;";
        this.TMatcherQN = this.TPackage + "IASMatcher";
        this.TMatcherDesc = String.format("L%s;", this.TMatcherQN);
        this.TPatternQN = this.TPackage + "IASPattern";
        this.TPatternDesc = String.format("L%s;", this.TPatternQN);
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

    public String getMethodTStringDesc() {
        return this.MethodTStringDesc;
    }

    public String getMethodTStringBuilderDesc() {
        return this.MethodTStringBuilderDesc;
    }

    public String getMethodTStringBufferDesc() {
        return MethodTStringBufferDesc;
    }

    public String getMethodTFormatterDesc() {
        return this.MethodTFormatterDesc;
    }

    public String getReflectionMethodProxyQN() {
        return this.ReflectionMethodProxyQN;
    }

    public String getToArrayProxyQN() {
        return ToArrayProxyQN;
    }
}
