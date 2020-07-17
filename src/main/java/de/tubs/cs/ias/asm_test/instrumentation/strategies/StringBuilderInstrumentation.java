package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBuilderInstrumentation extends AbstractInstrumentation {
    protected final TaintStringConfig stringConfig;

    public StringBuilderInstrumentation(TaintStringConfig configuration) {
        super(Constants.StringBuilderDesc, configuration.getTStringBuilderDesc(), Constants.StringBuilderQN, configuration.getTStringBuilderQN(), Constants.TStringBuilderToStringBuilderName);
        this.stringConfig = configuration;
    }

    @Override
    public String instrumentDesc(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.strBuilderPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTStringBuilderDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.strBuilderPattern.matcher(returnType).replaceAll(this.stringConfig.getTStringBuilderDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }
}
