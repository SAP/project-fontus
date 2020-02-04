package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatterInstrumentation implements InstrumentationStrategy {
    private static final Pattern FORMATTER_QN_MATCHER = Pattern.compile(Constants.FormatterQN, Pattern.LITERAL);

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.FormatterDesc, Constants.TFormatterDesc);
    }

    @Override
    public String instrumentQN(String qn) {
        return FORMATTER_QN_MATCHER.matcher(qn).replaceAll(Matcher.quoteReplacement(Constants.TFormatterQN));
    }

    @Override
    public String instrumentDesc(String desc) {
        return Constants.formatterPattern.matcher(desc).replaceAll(Constants.TFormatterDesc);
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.fixup(Constants.FormatterQN))) {
            return Optional.of(Utils.fixup(Constants.TFormatterQN));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String typeName) {
        return Constants.FormatterDesc.endsWith(typeName);
    }
}
