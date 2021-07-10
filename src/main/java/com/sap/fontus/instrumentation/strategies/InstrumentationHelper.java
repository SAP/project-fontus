package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.config.TaintStringConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class InstrumentationHelper {
    private final Collection<InstrumentationStrategy> strategies = new ArrayList<>(5);
    private static InstrumentationHelper INSTANCE;

    public static InstrumentationHelper getInstance(TaintStringConfig configuration) {
        if (INSTANCE == null) {
            INSTANCE = new InstrumentationHelper(configuration);
        }
        return INSTANCE;
    }

    private InstrumentationHelper(TaintStringConfig configuration) {
        this.strategies.add(new FormatterInstrumentation(configuration));
        this.strategies.add(new MatcherInstrumentation(configuration));
        this.strategies.add(new PatternInstrumentation(configuration));
        this.strategies.add(new StringInstrumentation(configuration));
        this.strategies.add(new StringBuilderInstrumentation(configuration));
        this.strategies.add(new StringBufferInstrumentation(configuration));
        this.strategies.add(new PropertiesStrategy(configuration));
        this.strategies.add(new ProxyInstrumentation());
        this.strategies.add(new DefaultInstrumentation(configuration));
    }

    public String instrumentQN(String qn) {
        String newQN = qn;
        for (InstrumentationStrategy is : this.strategies) {
            newQN = is.instrumentQN(newQN);
            if (!qn.equals(newQN)) {
                break;
            }
        }
        return newQN;
    }


    /**
     * This instruments the descriptors for normal application classes (uses the actual taintaware classes (e.g. IASString))
     */
    public String instrumentForNormalCall(String desc) {
        return this.instrumentForNormalCall(Descriptor.parseDescriptor(desc)).toDescriptor();
    }

    /**
     * This instruments the descriptors for normal application classes (uses the actual taintaware classes (e.g. IASString))
     */
    public Descriptor instrumentForNormalCall(Descriptor desc) {
        Descriptor newDesc = desc;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.instrumentForNormalCall(newDesc);
        }
        return newDesc;
    }

    public String uninstrumentNormalCall(String typeDescriptor) {
        String newDesc = typeDescriptor;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.uninstrumentNormalCall(newDesc);
        }
        return newDesc;
    }

    public Descriptor uninstrumentNormalCall(Descriptor typeDescriptor) {
        String newDesc = typeDescriptor.toDescriptor();
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.uninstrumentNormalCall(newDesc);
        }
        return Descriptor.parseDescriptor(newDesc);
    }

    public String instrumentDescForIASCall(String desc) {
        String newDesc = desc;
        for (InstrumentationStrategy is : this.strategies) {
            newDesc = is.instrumentDescForIASCall(newDesc);
        }
        return newDesc;
    }

    public String translateClassName(String clazzName) {

        for (InstrumentationStrategy is : this.strategies) {
            Optional<String> os = is.translateClassName(clazzName);
            if (os.isPresent()) {
                return os.get();
            }
        }
        return clazzName;
    }

    public boolean canHandleType(String typeDescriptor) {
        for (InstrumentationStrategy is : this.strategies) {
            if (is.handlesType(typeDescriptor)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInstrumented(String descriptor) {
        for (InstrumentationStrategy is : this.strategies) {
            if (is.isInstrumented(descriptor)) {
                return true;
            }
        }
        return false;
    }
}
