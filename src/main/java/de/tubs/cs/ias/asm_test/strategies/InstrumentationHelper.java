package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class InstrumentationHelper {
    private static final Collection<InstrumentationStrategy> strategies = new ArrayList<>(4);

    static {
        strategies.add(new StringInstrumentation());
        strategies.add(new StringBuilderInstrumentation());
        strategies.add(new StringBufferInstrumentation());
        strategies.add(new DefaultInstrumentation());
    }

    public static String instrumentQN(String qn) {
        String newQN = qn;
        for(InstrumentationStrategy is : strategies) {
            newQN = is.instrumentQN(newQN);
        }
        return newQN;
    }

    public static Descriptor instrument(Descriptor desc) {
        Descriptor newDesc = desc;
        for(InstrumentationStrategy is : strategies) {
            newDesc = is.instrument(newDesc);
        }
        return newDesc;
    }

    public static String instrumentDesc(String desc) {
        String newDesc = desc;
        for(InstrumentationStrategy is : strategies) {
            newDesc = is.instrumentDesc(newDesc);
        }
        return newDesc;
    }
    public static String translateClassName(String clazzName) {

        for(InstrumentationStrategy is : strategies) {
            Optional<String> os = is.translateClassName(clazzName);
            if(os.isPresent()) { return os.get(); }
        }
        return clazzName;
    }

    public static boolean canHandleType(String type) {
        for(InstrumentationStrategy is : strategies) {
            if(is.handlesType(type)) { return true; }
        }
        return false;
    }
}
