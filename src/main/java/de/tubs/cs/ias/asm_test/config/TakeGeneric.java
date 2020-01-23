package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "takeGeneric")
public class TakeGeneric {
    @JacksonXmlElementWrapper(localName = "functions")
    @XmlElement(name = "takes")
    private final List<TakesGeneric> functions;

    @JsonCreator
    public TakeGeneric(@JsonProperty("*") List<TakesGeneric> functions) {
        this.functions = functions;
    }

    public List<TakesGeneric> getFunction() {
        return Collections.unmodifiableList(this.functions);
    }

    public boolean contains(TakesGeneric fc) {
        return this.functions.contains(fc);
    }

    public int size() {
        return this.functions.size();
    }

    public void append(TakeGeneric other) {
	this.functions.addAll(other.functions);
    }

}
