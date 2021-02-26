package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.asm.FunctionCall;

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

    @XmlElement
    private final String category;

    public Sink() {
        this.name = "";
        this.function = new FunctionCall();
        this.parameters = new ArrayList<>();
        this.category = null;
    }

    public Sink(String name, FunctionCall functionCall, List<SinkParameter> parameters, String category) {
        this.name = name;
        this.function = functionCall;
        this.parameters = parameters;
        this.category = category;
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

    public String getCategory() {
        return category;
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
                '}';
    }
}
