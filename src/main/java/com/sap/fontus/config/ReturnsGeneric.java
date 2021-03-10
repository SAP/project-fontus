package com.sap.fontus.config;

import com.sap.fontus.asm.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "returnsGeneric")
public class ReturnsGeneric {

    @XmlElement(name = "function")
    private FunctionCall functionCall;

    @XmlElement
    private String converter;

    public FunctionCall getFunctionCall() {
        return this.functionCall;
    }

    public String getConverter() {
        return this.converter;
    }
}
