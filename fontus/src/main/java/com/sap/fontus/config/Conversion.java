package com.sap.fontus.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "conversion")
public class Conversion {
    @XmlElement
    private int index;
    @XmlElement
    private String converter;

    public int getIndex() {
        return this.index;
    }

    public String getConverter() {
        return this.converter;
    }
}
