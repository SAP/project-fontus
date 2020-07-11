package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatterInstrumentation implements InstrumentationStrategy {
    private static final Pattern FORMATTER_QN_MATCHER = Pattern.compile(Constants.FormatterQN, Pattern.LITERAL);
    protected final TaintStringConfig stringConfig;

    public FormatterInstrumentation(TaintStringConfig configuration) {
        this.stringConfig = configuration;
    }

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.FormatterDesc, this.stringConfig.getTFormatterDesc());
    }

    @Override
    public String instrumentQN(String qn) {
        return FORMATTER_QN_MATCHER.matcher(qn).replaceAll(Matcher.quoteReplacement(this.stringConfig.getTFormatterQN()));
    }

    @Override
    public String instrumentDesc(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.formatterPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTFormatterDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.formatterPattern.matcher(returnType).replaceAll(this.stringConfig.getTFormatterDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.fixup(Constants.FormatterQN))) {
            return Optional.of(Utils.fixup(this.stringConfig.getTFormatterQN()));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String typeName) {
        return Constants.FormatterDesc.endsWith(typeName);
    }
}
