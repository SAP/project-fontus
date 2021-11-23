package com.sap.fontus.config;

import com.sap.fontus.asm.FunctionCall;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import sun.security.x509.AttributeNameEnumeration;

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

    @JacksonXmlElementWrapper(localName = "vendors")
    @XmlElement(name = "vendor")
    private final List<String> vendors;

    @JacksonXmlElementWrapper(localName = "purposes")
    @XmlElement(name = "purpose")
    private final List<String> purposes;

    public Sink() {
        this.name = "";
        this.function = new FunctionCall();
        this.parameters = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.vendors = new ArrayList<>();
        this.purposes = new ArrayList<>();
    }

    public Sink(String name, FunctionCall functionCall, List<SinkParameter> parameters, List<String> categories, List<String> vendors, List<String> purposes) {
        this.name = name;
        this.function = functionCall;
        this.parameters = parameters;
        this.categories = categories;
        this.vendors = vendors;
        this.purposes = purposes;
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

    public List<String> getVendors() {
        return Collections.unmodifiableList(this.vendors);
    }

    public List<String> getPurposes() {
        return Collections.unmodifiableList(this.purposes);
    }

    public List<String> getCategories() {
        return Collections.unmodifiableList(this.categories);
    }

    public FunctionCall getFunction() {
        return this.function;
    }

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
                '}';
    }
}
