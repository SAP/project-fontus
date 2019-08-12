package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBuilderInstrumentation implements InstrumentationStrategy {
    private static final Pattern STRING_BUILDER_QN_MATCHER = Pattern.compile(Constants.StringBuilderQN, Pattern.LITERAL);

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.StringBuilderDesc, Constants.TStringBuilderDesc);

    }

    @Override
    public String instrumentQN(String qn) {
        return  STRING_BUILDER_QN_MATCHER.matcher(qn).replaceAll(Matcher.quoteReplacement(Constants.TStringBuilderQN));
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.fixup(Constants.StringBuilderQN))) {
            return Optional.of(Utils.fixup(Constants.TStringBuilderQN));
        }
        return Optional.empty();
    }
}
