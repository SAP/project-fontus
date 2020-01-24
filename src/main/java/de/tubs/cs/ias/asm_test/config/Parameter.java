package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "parameter")
public class Parameter {

    @XmlElement
    private int index;

    public Parameter() {
        this.index = -1;
    }

    public Parameter(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
