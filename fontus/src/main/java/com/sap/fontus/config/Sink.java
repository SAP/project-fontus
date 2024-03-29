package com.sap.fontus.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.fontus.asm.FunctionCall;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.sap.fontus.config.abort.Abort;
import com.sap.fontus.config.abort.MultiAbort;

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

    @JacksonXmlElementWrapper(localName = "positions")
    @XmlElement(name = "position")
    private final List<Position> positions;

    @XmlElement(name = "tainthandler")
    @JsonProperty("tainthandler")
    private final FunctionCall taintHandler;

    public Sink() {
        this.name = "";
        this.function = new FunctionCall();
        this.parameters = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.dataProtection = new DataProtection();
        this.positions = new ArrayList<>();
        this.taintHandler = FunctionCall.EmptyFunctionCall;
    }

    public Sink(String name, FunctionCall functionCall, List<SinkParameter> parameters, List<String> categories, DataProtection dataProtection, FunctionCall taintHandler, List<Position> positions) {
        this.name = name;
        this.function = functionCall;
        this.parameters = parameters;
        this.categories = categories;
        this.dataProtection = dataProtection;
        this.taintHandler = taintHandler;
        this.positions = positions;
    }

    public Sink(String name, FunctionCall functionCall, List<SinkParameter> parameters, List<String> categories, DataProtection dataProtection) {
        this.name = name;
        this.function = functionCall;
        this.parameters = parameters;
        this.categories = categories;
        this.dataProtection = dataProtection;
        this.positions = new ArrayList<>();
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

    public DataProtection getDataProtection() { return this.dataProtection; }

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

    @JsonIgnore
    public Abort getAbortFromSink() {
        List<Abort> l = new ArrayList<>();
        for (String abortName : this.dataProtection.getAborts()) {
            Abort a = Abort.parse(abortName);
            if (a != null) {
                l.add(a);
            }
        }
        return new MultiAbort(l);
    }

    public List<Position> getPositions() {
        return this.positions;
    }

    @Override
    public String toString() {
        return "Sink{" +
                "name='" + this.name + '\'' +
                ", function=" + this.function +
                ", parameters=" + this.parameters +
                ", categories=" + this.categories +
                ", dataProtection=" + this.dataProtection +
                ", taintHandler=" + this.taintHandler +
                ", positions=" + this.positions +
                '}';
    }
}
