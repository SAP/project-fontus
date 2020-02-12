package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class InstrumentationHelper {
    private final Collection<InstrumentationStrategy> strategies = new ArrayList<>(5);
    private static InstrumentationHelper INSTANCE;

    public static InstrumentationHelper getInstance(TaintStringConfig configuration) {
        if(INSTANCE == null) {
            INSTANCE = new InstrumentationHelper(configuration);
        }
        return INSTANCE;
    }


    private InstrumentationHelper(TaintStringConfig configuration) {
        strategies.add(new FormatterInstrumentation(configuration));
        strategies.add(new StringInstrumentation(configuration));
        strategies.add(new StringBuilderInstrumentation(configuration));
        strategies.add(new StringBufferInstrumentation(configuration));
        strategies.add(new DefaultInstrumentation(configuration));
    }

    public String instrumentQN(String qn) {
        String newQN = qn;
        for (InstrumentationStrategy is : strategies) {
            newQN = is.instrumentQN(newQN);
        }
        return newQN;
    }

    public Descriptor instrument(Descriptor desc) {
        Descriptor newDesc = desc;
        for (InstrumentationStrategy is : strategies) {
            newDesc = is.instrument(newDesc);
        }
        return newDesc;
    }

    public String instrumentDesc(String desc) {
        String newDesc = desc;
        for (InstrumentationStrategy is : strategies) {
            newDesc = is.instrumentDesc(newDesc);
        }
        return newDesc;
    }

    public String translateClassName(String clazzName) {

        for (InstrumentationStrategy is : strategies) {
            Optional<String> os = is.translateClassName(clazzName);
            if (os.isPresent()) {
                return os.get();
            }
        }
        return clazzName;
    }

    public boolean canHandleType(String type) {
        for (InstrumentationStrategy is : strategies) {
            if (is.handlesType(type)) {
                return true;
            }
        }
        return false;
    }
}
