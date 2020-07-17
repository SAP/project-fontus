package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatterInstrumentation extends AbstractInstrumentation {
    private final TaintStringConfig stringConfig;

    public FormatterInstrumentation(TaintStringConfig configuration) {
        super(Constants.FormatterDesc, configuration.getTFormatterDesc(), Constants.FormatterQN, configuration.getTFormatterQN(), Constants.TFormatterToFormatterName);
        this.stringConfig = configuration;
    }

    @Override
    public String instrumentDesc(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.formatterPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTFormatterDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.formatterPattern.matcher(returnType).replaceAll(this.stringConfig.getTFormatterDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }
}
