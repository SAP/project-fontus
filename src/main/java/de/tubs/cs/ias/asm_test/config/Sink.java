package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.FunctionCall;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "sink")
public class Sink {

    @XmlElement
    private String name;

    @XmlElement
    private FunctionCall function;

    @JacksonXmlElementWrapper(localName = "parameters")    
    @XmlElement(name = "parameter")
    private final List<Parameter> parameters;

    public Sink() {
        this.name = "";
	    this.function = new FunctionCall();
        this.parameters = new ArrayList<Parameter>();
    }

    public Sink(String name, FunctionCall functionCall, List<Parameter> parameters) {
        this.name = name;
	    this.function = functionCall;
        this.parameters = parameters;
    }
   
    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public FunctionCall getFunction() {
        return this.function;
    }

    public String getName() {
        return this.name;
    }

}
