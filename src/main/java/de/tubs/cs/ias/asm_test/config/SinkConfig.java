package de.tubs.cs.ias.asm_test.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import de.tubs.cs.ias.asm_test.FunctionCall;

public class SinkConfig {

    public SinkConfig() {
        this.sinks = new ArrayList<>();
    }

    public SinkConfig(List<Sink> sinks) {
        this.sinks = sinks;
    }

    public void append(SinkConfig sinkconfig) {
        this.sinks.addAll(sinkconfig.sinks);
    }

    public Sink getSinkForFunction(FunctionCall fc) {
        for (Sink s : this.sinks) {
            if (s.getFunction().equals(fc)) {
                return s;
            }
        }
        return null;
    }

    public boolean containsFunction(FunctionCall fc) {
        return (this.getSinkForFunction(fc) != null);
    }

    public List<Sink> getSinks() {
        return Collections.unmodifiableList(this.sinks);
    }

    @JacksonXmlElementWrapper(localName = "sinks")
    @XmlElement(name = "sink")
    private final List<Sink> sinks;

}
