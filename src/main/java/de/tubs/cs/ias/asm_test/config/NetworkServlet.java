package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "networkServlet")
public class NetworkServlet {
    @XmlElement
    private final String name;

    @XmlElement
    private final FunctionCall function;

    @JacksonXmlElementWrapper(localName = "parameters")
    @XmlElement(name = "parameter")
    private final List<NetworkServletParameter> parameters;

    private final String category;

    public NetworkServlet() {
        this.name = "";
        this.function = new FunctionCall();
        this.parameters = new ArrayList<>();
        this.category = null;
    }

    public NetworkServlet(String name, FunctionCall functionCall, List<NetworkServletParameter> parameters, String category) {
        this.name = name;
        this.function = functionCall;
        this.parameters = parameters;
        this.category = category;
    }

    public List<NetworkServletParameter> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public NetworkServletParameter findParameter(int i) {
        for (NetworkServletParameter p : this.parameters) {
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
        return "NetworkServlet{" +
                "name='" + name + '\'' +
                ", function=" + function +
                ", parameters=" + parameters +
                '}';
    }
}
