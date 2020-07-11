package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "takes")
public class TakesGeneric {

    @XmlElement(name = "function")
    private final FunctionCall functionCall;

    @JacksonXmlElementWrapper(localName = "conversions")
    //@XmlElement(name = "conversion")
    private final List<Conversion> conversions;


    @JsonCreator
    public TakesGeneric(@JsonProperty("function") FunctionCall functionCall, @JsonProperty("conversions") List<Conversion> conversions) {
        this.functionCall = functionCall;
        this.conversions = conversions;
    }

    public List<Conversion> getConversions() {
        return Collections.unmodifiableList(this.conversions);
    }

    public Conversion getConversionAt(int index) {
        for (Conversion c : this.conversions) {
            if (c.getIndex() == index) return c;
        }
        return null;
    }

    public FunctionCall getFunctionCall() {
        return this.functionCall;
    }

}
