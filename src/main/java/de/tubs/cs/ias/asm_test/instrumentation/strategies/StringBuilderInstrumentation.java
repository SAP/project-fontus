package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBuilderInstrumentation implements InstrumentationStrategy {
    private static final Pattern STRING_BUILDER_QN_MATCHER = Pattern.compile(Constants.StringBuilderQN, Pattern.LITERAL);
    protected final TaintStringConfig stringConfig;

    public StringBuilderInstrumentation(TaintStringConfig configuration) {
        this.stringConfig = configuration;
    }

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.StringBuilderDesc, this.stringConfig.getTStringBuilderDesc());

    }

    @Override
    public String instrumentQN(String qn) {
        return  STRING_BUILDER_QN_MATCHER.matcher(qn).replaceAll(Matcher.quoteReplacement(this.stringConfig.getTStringBuilderQN()));
    }

    @Override
    public String instrumentDesc(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.strBuilderPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTStringBuilderDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.strBuilderPattern.matcher(returnType).replaceAll(this.stringConfig.getTStringBuilderDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.fixup(Constants.StringBuilderQN))) {
            return Optional.of(Utils.fixup(this.stringConfig.getTStringBuilderQN()));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String typeName) {
        return typeName.endsWith(Constants.StringBuilderDesc);
    }
}
