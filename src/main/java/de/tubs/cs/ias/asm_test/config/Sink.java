package de.tubs.cs.ias.asm_test.config;

import de.tubs.cs.ias.asm_test.FunctionCall;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "sink")
public class Sink {

    @XmlElement(name = "function")
    private FunctionCall functionCall;

    @JacksonXmlElementWrapper(localName = "parameters", useWrapping = false)    
    @XmlElement(name = "parameter")
    private final List<Parameter> parameters;

    @JsonCreator
    public Sink(@JsonProperty("function") FunctionCall functionCall, @JsonProperty("parameters") List<Parameter> parameters) {
	this.functionCall = functionCall;
        this.parameters = parameters;
    }
   
    public FunctionCall getFunctionCall() {
        return this.functionCall;
    }

}
