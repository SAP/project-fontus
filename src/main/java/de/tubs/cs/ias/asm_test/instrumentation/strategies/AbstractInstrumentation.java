package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import org.objectweb.asm.Type;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractInstrumentation implements InstrumentationStrategy {
    private final Pattern qnMatcher;
    protected final String origDesc;
    protected final String taintedDesc;
    protected final String taintedQN;
    protected final String origQN;
    protected final Pattern descPattern;
    protected final String getOriginalTypeMethod;

    protected AbstractInstrumentation(String origDesc, String taintedDesc, String origQN, String taintedQN, String getOriginalTypeMethod) {
        this.origDesc = origDesc;
        this.taintedDesc = taintedDesc;
        this.taintedQN = taintedQN;
        this.origQN = origQN;
        this.qnMatcher = Pattern.compile(origQN, Pattern.LITERAL);
        this.descPattern = Pattern.compile(origDesc);
        this.getOriginalTypeMethod = getOriginalTypeMethod;
    }

    protected AbstractInstrumentation(Class<?> origClass, Class<?> taintedClass, String getOriginalTypeMethod) {
        this.origDesc = Type.getType(origClass).getDescriptor();
        this.origQN = Type.getType(origClass).getInternalName();
        this.taintedDesc = Type.getType(taintedClass).getDescriptor();
        this.taintedQN = Type.getType(taintedClass).getInternalName();
        this.qnMatcher = Pattern.compile(origQN, Pattern.LITERAL);
        this.descPattern = Pattern.compile(origDesc);
        this.getOriginalTypeMethod = getOriginalTypeMethod;
    }

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(this.origDesc, this.taintedDesc);
    }

    @Override
    public String instrumentQN(String qn) {
        return this.qnMatcher.matcher(qn).replaceAll(Matcher.quoteReplacement(this.taintedQN));
    }

    @Override
    public String instrumentDesc(String desc) {
        return this.descPattern.matcher(desc).replaceAll(this.taintedDesc);
    }

    @Override
    public Optional<String> translateClassName(String className) {
        if (className.equals(Utils.slashToDot(this.origQN))) {
            return Optional.of(Utils.slashToDot(this.taintedQN));
        }
        return Optional.empty();
    }

    @Override
    public boolean handlesType(String typeName) {
        return typeName.endsWith(this.origDesc);
    }

    @Override
    public String getGetOriginalTypeMethod() {
        return getOriginalTypeMethod;
    }
}
