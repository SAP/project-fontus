package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import de.tubs.cs.ias.asm_test.FunctionCall;

import javax.xml.bind.annotation.*;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.List;

@XmlRootElement(name = "sinks")
public class Sinks {

    @JacksonXmlElementWrapper(useWrapping = false, localName = "functions")
    @XmlElement(name = "sink")
    private final List<Sink> sinks;

    @JsonCreator
    public Sinks(@JsonProperty("*") List<Sink> sinks) {
        this.sinks = sinks;
    }

    public List<Sink> getSinks() {
        return Collections.unmodifiableList(this.sinks);
    }

    public List<FunctionCall> getFunction() {
        return this.sinks.stream().map(u -> u.getFunctionCall()).collect(Collectors.toList());
    }

    public boolean contains(Sink sink) {
        return this.sinks.contains(sink);
    }

    public boolean contains(FunctionCall fc) {
	return this.getFunction().contains(fc);
    }

    public int size() {
        return this.sinks.size();
    }

    public void append(Sinks other) {
	this.sinks.addAll(other.sinks);
    }

}
