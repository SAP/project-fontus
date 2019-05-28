package de.tubs.cs.ias.asm_test;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.xml.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "sources")
public class Sources {


    @JacksonXmlElementWrapper(localName = "functions")
    @XmlElement(name = "function")
    private final List<FunctionCall> functions;

    @JsonCreator
    public Sources(@JsonProperty("*")List<FunctionCall> functions) {
        this.functions = functions;
    }

    public List<FunctionCall> getFunction() {
        return Collections.unmodifiableList(this.functions);
    }

    public boolean contains(FunctionCall fc) {
        return this.functions.contains(fc);
    }
}
