package com.sap.fontus.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "parameter")
public class SinkParameter {

    @XmlElement
    private final int index;

    public SinkParameter() {
        this.index = -1;
    }

    public SinkParameter(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return "SinkParameter{" +
                "index=" + index +
                '}';
    }
}
