package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "source")
public class Source {

    @XmlElement
    private final String name;

    @XmlElement
    private final FunctionCall function;

    public Source() {
        this.name = "";
        this.function = new FunctionCall();
    }

    public Source(String name, FunctionCall functionCall) {
        this.name = name;
        this.function = functionCall;
    }

    public FunctionCall getFunction() {
        return this.function;
    }

    public String getName() {
        return this.name;
    }

}
