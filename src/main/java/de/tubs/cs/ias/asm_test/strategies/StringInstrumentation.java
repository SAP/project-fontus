package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringInstrumentation implements InstrumentationStrategy {
    private static final Pattern STRING_QN_MATCHER = Pattern.compile(Constants.StringQN, Pattern.LITERAL);
    protected final TaintStringConfig stringConfig;

    public StringInstrumentation(TaintStringConfig configuration) {
        this.stringConfig = configuration;
    }

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.StringDesc, this.stringConfig.getTStringDesc());
    }

    @Override
    public String instrumentQN(String qn) {
        return STRING_QN_MATCHER.matcher(qn).replaceAll(Matcher.quoteReplacement(this.stringConfig.getTStringQN()));
    }

    @Override
    public String instrumentDesc(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.strPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTStringDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.strPattern.matcher(returnType).replaceAll(this.stringConfig.getTStringDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.fixup(Constants.StringQN))) {
            return Optional.of(Utils.fixup(this.stringConfig.getTStringQN()));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String typeName) {
        return Constants.StringDesc.endsWith(typeName);
    }
}
