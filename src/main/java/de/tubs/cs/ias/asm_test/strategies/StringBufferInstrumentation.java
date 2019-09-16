package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBufferInstrumentation implements InstrumentationStrategy {
    private static final Pattern STRING_BUFFER_QN_MATCHER = Pattern.compile(Constants.StringBufferQN, Pattern.LITERAL);

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.StringBufferDesc, Constants.TStringBufferDesc);
    }

    @Override
    public String instrumentQN(String qn) {
        return STRING_BUFFER_QN_MATCHER.matcher(qn).replaceAll(Matcher.quoteReplacement(Constants.TStringBufferQN));
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.fixup(Constants.StringBufferQN))) {
            return Optional.of(Utils.fixup(Constants.TStringBufferQN));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String typeName) {
        return Constants.StringBufferDesc.endsWith(typeName);
    }
}
