package de.tubs.cs.ias.asm_test.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "parameter")
public class SinkParameter {

    @XmlElement
    private int index;

    public SinkParameter() {
        this.index = -1;
    }

    public SinkParameter(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
