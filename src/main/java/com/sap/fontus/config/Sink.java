package com.sap.fontus.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.fontus.asm.FunctionCall;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "sink")
public class Sink {

    @XmlElement
    private final String name;

    @XmlElement
    private final FunctionCall function;

    @JacksonXmlElementWrapper(localName = "parameters")
    @XmlElement(name = "parameter")
    private final List<SinkParameter> parameters;

    @JacksonXmlElementWrapper(localName = "categories")
    @XmlElement(name = "category")
    private final List<String> categories;

    @XmlElement(name = "dataProtection")
    private final DataProtection dataProtection;

    @XmlElement(name = "tainthandler")
    @JsonProperty("tainthandler")
    private final FunctionCall taintHandler;

    public Sink() {
        this.name = "";
        this.function = new FunctionCall();
        this.parameters = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.dataProtection = new DataProtection();
        this.taintHandler = FunctionCall.EmptyFunctionCall;
    }

    public Sink(String name, FunctionCall functionCall, List<SinkParameter> parameters, List<String> categories, DataProtection dataProtection, FunctionCall taintHandler) {
        this.name = name;
        this.function = functionCall;
        this.parameters = parameters;
        this.categories = categories;
        this.dataProtection = dataProtection;
        this.taintHandler = taintHandler;
    }

    public Sink(String name, FunctionCall functionCall, List<SinkParameter> parameters, List<String> categories, DataProtection dataProtection) {
        this.name = name;
        this.function = functionCall;
        this.parameters = parameters;
        this.categories = categories;
        this.dataProtection = dataProtection;
        this.taintHandler = FunctionCall.EmptyFunctionCall;
    }

    public List<SinkParameter> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public SinkParameter findParameter(int i) {
        for (SinkParameter p : this.parameters) {
            if (p.getIndex() == i) {
                return p;
            }
        }
        return null;
    }

    public DataProtection getDataProtection() { return dataProtection; }

    public List<String> getCategories() {
        return Collections.unmodifiableList(this.categories);
    }

    public FunctionCall getFunction() {
        return this.function;
    }

    public FunctionCall getTaintHandler() { return this.taintHandler; }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "Sink{" +
                "name='" + name + '\'' +
                ", function=" + function +
                ", parameters=" + parameters +
                ", categories=" + categories +
                ", dataProtection=" + dataProtection +
                ", taintHandler=" + taintHandler +
                '}';
    }
}
