package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBuilderInstrumentation implements InstrumentationStrategy {
    private static final Pattern STRING_BUILDER_QN_MATCHER = Pattern.compile(Constants.StringBuilderQN, Pattern.LITERAL);
    private final TaintStringConfig stringConfig = Configuration.instance.getTaintStringConfig();

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.StringBuilderDesc, stringConfig.getTStringBuilderDesc());

    }

    @Override
    public String instrumentQN(String qn) {
        return  STRING_BUILDER_QN_MATCHER.matcher(qn).replaceAll(Matcher.quoteReplacement(stringConfig.getTStringBuilderQN()));
    }

    @Override
    public String instrumentDesc(String desc) {
        return Constants.strBuilderPattern.matcher(desc).replaceAll(stringConfig.getTStringBuilderDesc());
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.fixup(Constants.StringBuilderQN))) {
            return Optional.of(Utils.fixup(stringConfig.getTStringBuilderQN()));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String typeName) {
        return Constants.StringBuilderDesc.endsWith(typeName);
    }
}
