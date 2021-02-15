package de.tubs.cs.ias.asm_test.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkServletConfig {
    public NetworkServletConfig() {
        this.networkServlets = new ArrayList<>();
    }

    public NetworkServletConfig(List<NetworkServlet> networkServlets) {
        this.networkServlets = networkServlets;
    }

    public void append(NetworkServletConfig httpServletConfig) {
        this.networkServlets.addAll(httpServletConfig.networkServlets);
    }

    public NetworkServlet getNetworkServletForFunction(FunctionCall fc) {
        for (NetworkServlet s : this.networkServlets) {
            if (s.getFunction().equals(fc)) {
                return s;
            }
        }
        return null;
    }

    public boolean containsFunction(FunctionCall fc) {
        return (this.getNetworkServletForFunction(fc) != null);
    }

    public List<NetworkServlet> getNetworkServlets() {
        return Collections.unmodifiableList(this.networkServlets);
    }

    @JacksonXmlElementWrapper(localName = "networkServlets")
    @XmlElement(name = "networkServlet")
    private final List<NetworkServlet> networkServlets;
}
