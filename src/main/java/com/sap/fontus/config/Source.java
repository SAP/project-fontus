package com.sap.fontus.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.fontus.asm.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "source")
public class Source {
    @Override
    public String toString() {
        return "Source{" +
                "name='" + name + '\'' +
                ", function=" + function +
                '}';
    }

    @XmlElement
    private final String name;

    @XmlElement
    private final FunctionCall function;

    @XmlElement(name = "tainthandler")
    @JsonProperty(value = "tainthandler")
    private final FunctionCall taintHandler;

    public Source() {
        this.name = "";
        this.function = new FunctionCall();
        this.taintHandler = FunctionCall.EmptyFunctionCall;
    }

    public Source(String name, FunctionCall functionCall) {
        this.name = name;
        this.function = functionCall;
        this.taintHandler = FunctionCall.EmptyFunctionCall;
    }

    public Source(String name, FunctionCall functionCall, FunctionCall taintHandler) {
        this.name = name;
        this.function = functionCall;
        this.taintHandler = taintHandler;
    }

    public FunctionCall getFunction() {
        return this.function;
    }

    public String getName() {
        return this.name;
    }

    public FunctionCall getTaintHandler() { return this.taintHandler; }

}
