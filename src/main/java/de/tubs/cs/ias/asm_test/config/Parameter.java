package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "parameter")
public class Parameter {

    @XmlElement
    private int index;

}
