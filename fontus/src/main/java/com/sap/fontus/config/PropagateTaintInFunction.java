package com.sap.fontus.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "propagate_in_func")
public class PropagateTaintInFunction {
    @XmlElement
    private final String name;

    @XmlElement
    private final MethodDeclaration method;
    @XmlElement(name = "arg_index")
    private final int argIndex;

    public PropagateTaintInFunction() {
        this.name = null;
        this.method = null;
        this.argIndex = -1;
    }

    public PropagateTaintInFunction(String name, MethodDeclaration function, int argIndex) {
        this.name = name;
        this.method = function;
        this.argIndex = argIndex;
    }

    public String getName() {
        return this.name;
    }

    public MethodDeclaration getMethod() {
        return this.method;
    }

    public int getArgIndex() {
        return this.argIndex;
    }

    @Override
    public String toString() {
        return String.format("PropagateTaintInFunction{name='%s', function=%s, argIndex=%d}", this.name, this.method, this.argIndex);
    }
}
