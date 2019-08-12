package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringInstrumentation implements InstrumentationStrategy {
    private static final Pattern STRING_QN_MATCHER = Pattern.compile(Constants.StringQN, Pattern.LITERAL);

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.StringDesc, Constants.TStringDesc);
    }

    @Override
    public String instrumentQN(String qn) {
        return STRING_QN_MATCHER.matcher(qn).replaceAll(Matcher.quoteReplacement(Constants.TStringQN));
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.fixup(Constants.StringQN))) {
            return Optional.of(Utils.fixup(Constants.TStringQN));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String typeName) {
        return Constants.StringDesc.endsWith(typeName);
    }
}
