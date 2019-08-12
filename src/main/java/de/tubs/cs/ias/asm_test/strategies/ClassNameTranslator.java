package de.tubs.cs.ias.asm_test.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class ClassNameTranslator {
    private static final Collection<InstrumentationStrategy> strategies = new ArrayList<>(4);

    static {
        strategies.add(new StringInstrumentation());
        strategies.add(new StringBuilderInstrumentation());
        strategies.add(new StringBufferInstrumentation());
        strategies.add(new DefaultInstrumentation());
    }

    public static String translateClassName(String clazzName) {

        for(InstrumentationStrategy is : strategies) {
            Optional<String> os = is.translateClassName(clazzName);
            if(os.isPresent()) { return os.get(); }
        }
        return clazzName;
    }

}
