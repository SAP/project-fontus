package com.sap.fontus.config;

import javax.xml.bind.annotation.XmlElement;

public class Purpose {

    @XmlElement
    private final String name;

    @XmlElement
    private final String description;

    @XmlElement
    private final String legal;


    public Purpose(String name, String description, String legal) {
        this.name = name;
        this.description = description;
        this.legal = legal;
    }

    public Purpose() {
        this.name = new String();
        this.description = new String();
        this.legal = new String();
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLegal() {
        return this.legal;
    }

}
