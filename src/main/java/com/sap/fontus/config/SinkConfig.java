package com.sap.fontus.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.sap.fontus.asm.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SinkConfig {

    @JacksonXmlElementWrapper(localName = "sinks")
    @XmlElement(name = "sink")
    private final List<Sink> sinks;

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

    public Sink getSinkForFunction(FunctionCall fc, Position pos) {
        for (Sink s : this.sinks) {
            if (s.getFunction().equals(fc)) {
                // If no position is specified in the config then this sink applies always
                if (s.getPositions().isEmpty() || pos == null) {
                    return s;
                } else {
                    // check if current position of this function call is a sink position
                    for (Position p : s.getPositions()) {
                        if(p.equals(pos)) {
                            return s;
                        }
                    }
                }
            }
        }
        // function call is NOT a sink
        return null;
    }

    public Sink getPositionDependentSinkForFunction(FunctionCall fc, int line) {
        for (Sink s : this.sinks) {
            if (s.getFunction().equals(fc)) { // && s.getPosition() == line) {
                return s;
            }
        }
        return null;
    }

    public Sink getSinkForFqn(String fqn) {
        for (Sink s : this.sinks) {
            if (s.getFunction().getFqn().equals(fqn)) {
                return s;
            }
        }
        return null;
    }

    public Sink getSinkForName(String name) {
        for (Sink s : this.sinks) {
            if (s.getName().equals(name)) {
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

    @Override
    public String toString() {
        return "SinkConfig{" +
                "sinks=" + sinks +
                '}';
    }
}
