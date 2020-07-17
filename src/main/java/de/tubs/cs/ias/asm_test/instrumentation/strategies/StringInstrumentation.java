package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringInstrumentation extends AbstractInstrumentation {
    protected final TaintStringConfig stringConfig;

    public StringInstrumentation(TaintStringConfig configuration) {
        super(Constants.StringDesc, configuration.getTStringDesc(), Constants.StringQN, configuration.getTStringQN(), Constants.TStringToStringName);
        this.stringConfig = configuration;
    }

    @Override
    public String instrumentDesc(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.strPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTStringDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.strPattern.matcher(returnType).replaceAll(this.stringConfig.getTStringDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }
}
