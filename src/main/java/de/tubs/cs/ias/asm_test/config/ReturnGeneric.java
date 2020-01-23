package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "returnGeneric")
public class ReturnGeneric {
    @JacksonXmlElementWrapper(localName = "functions")
    @XmlElement(name = "returns")
    private final List<ReturnsGeneric> functions;

    @JsonCreator
    public ReturnGeneric(@JsonProperty("*") List<ReturnsGeneric> functions) {
        this.functions = functions;
    }

    public List<ReturnsGeneric> getFunction() {
        return Collections.unmodifiableList(this.functions);
    }

    public boolean contains(ReturnsGeneric fc) {
        return this.functions.contains(fc);
    }

    public int size() {
        return this.functions.size();
    }

    public void append(ReturnGeneric other) {
	this.functions.addAll(other.functions);
    }
}
