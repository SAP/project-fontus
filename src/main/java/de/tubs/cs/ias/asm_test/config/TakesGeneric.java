package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "takes")
public class TakesGeneric {

    @XmlElement(name = "function")
    private FunctionCall functionCall;
    @XmlElement
    private int index;
    @XmlElement
    private String converter;

    public int getIndex() {
        return this.index;
    }

    public FunctionCall getFunctionCall() {
        return this.functionCall;
    }

    public String getConverter() {
        return this.converter;
    }
}
