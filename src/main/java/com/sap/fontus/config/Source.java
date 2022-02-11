package com.sap.fontus.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.sap.fontus.asm.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "source")
public class Source {

    @XmlElement
    private final String name;

    @XmlElement
    private final FunctionCall function;

    @XmlElement(name = "tainthandler")
    @JsonProperty("tainthandler")
    private final FunctionCall taintHandler;

    @JacksonXmlElementWrapper(localName = "allowed_callers")
    @XmlElement(name = "allowed_callers")
    private final List<FunctionCall> allowedCallers;

    @JacksonXmlElementWrapper(localName = "pass_locals")
    @XmlElement(name = "pass_locals")
    private final List<Integer> passLocals;

    public Source() {
        this.name = "";
        this.function = new FunctionCall();
        this.taintHandler = FunctionCall.EmptyFunctionCall;
        this.allowedCallers = new ArrayList<>();
        this.passLocals = new ArrayList<>();
    }

    public Source(String name, FunctionCall functionCall) {
        this.name = name;
        this.function = functionCall;
        this.taintHandler = FunctionCall.EmptyFunctionCall;
        this.allowedCallers = new ArrayList<>();
        this.passLocals = new ArrayList<>();
    }

    public Source(String name, FunctionCall functionCall, FunctionCall taintHandler) {
        this.name = name;
        this.function = functionCall;
        this.taintHandler = taintHandler;
        this.allowedCallers = new ArrayList<>();
        this.passLocals = new ArrayList<>();
    }


    public Source(String name, FunctionCall functionCall, FunctionCall taintHandler, List<FunctionCall> allowedCallers) {
        this.name = name;
        this.function = functionCall;
        this.taintHandler = taintHandler;
        this.allowedCallers = allowedCallers;
        this.passLocals = new ArrayList<>();
    }


    public Source(String name, FunctionCall functionCall, FunctionCall taintHandler, List<FunctionCall> allowedCallers, List<Integer> passLocals) {
        this.name = name;
        this.function = functionCall;
        this.taintHandler = taintHandler;
        this.allowedCallers = allowedCallers;
        this.passLocals = passLocals;
    }

    public FunctionCall getFunction() {
        return this.function;
    }

    public String getName() {
        return this.name;
    }

    public FunctionCall getTaintHandler() { return this.taintHandler; }

    public List<FunctionCall> getAllowedCallers() {
        return this.allowedCallers;
    }

    public List<Integer> getPassLocals() {
        return this.passLocals;
    }

    @Override
    public String toString() {
        return String.format("Source{name='%s', function=%s, taintHandler=%s, allowedCallers=%s, passLocals=%s}", this.name, this.function, this.taintHandler, this.allowedCallers, this.passLocals);
    }
}
